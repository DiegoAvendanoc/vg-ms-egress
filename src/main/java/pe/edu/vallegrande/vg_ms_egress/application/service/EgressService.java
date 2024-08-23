package pe.edu.vallegrande.vg_ms_egress.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.vallegrande.vg_ms_egress.application.webclient.NotificationWebClient;
import pe.edu.vallegrande.vg_ms_egress.application.webclient.StorageWebClient;
import pe.edu.vallegrande.vg_ms_egress.application.webclient.UserWebClient;
import pe.edu.vallegrande.vg_ms_egress.domain.dto.*;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Category;
import pe.edu.vallegrande.vg_ms_egress.domain.model.Egress;
import pe.edu.vallegrande.vg_ms_egress.domain.repository.CategoryRepository;
import pe.edu.vallegrande.vg_ms_egress.domain.repository.EgressRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static pe.edu.vallegrande.vg_ms_egress.application.util.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EgressService {

    private final UserWebClient userWebClient;
    private final NotificationWebClient notificationWebClient;
    private final CategoryRepository categoryRepository;
    private final EgressRepository egressRepository;
    private final StorageWebClient storageWebClient;


    public Flux<Egress> listAllEgress() {
        return egressRepository.findAll()
                .flatMap(egress -> retrieveUsersList(egress)
                        .flatMap(tuple -> {
                            egress.setUser(tuple.getT1());
                            egress.setPersonConfirmed(tuple.getT2());
                            return retrieveCategory(egress)
                                    .map(category -> {
                                        egress.setCategory(category);
                                        return egress;
                                    })
                                    .defaultIfEmpty(egress);
                        }));
    }

    private Mono<Category> retrieveCategory(Egress egress) {
        if (egress.getCategoryId() != null) {
            return categoryRepository.findById(egress.getCategoryId());
        } else {
            return Mono.empty();
        }
    }

    public Mono<ResponseEntity<Egress>> createEgress(UserEgress userDto, MultipartFile[] files) {
        Egress egress = buildEgressFromDto(userDto);

        return storageWebClient.uploadFiles(files, FOLDER_NAME, egress.getPersonId(), egress.getEgressId())
                .flatMap(urls -> {
                    egress.setFileUrls(urls);
                    return egressRepository.save(egress);
                })
                .map(savedEgress -> new ResponseEntity<>(savedEgress, HttpStatus.CREATED))
                .onErrorResume(e -> {
                    log.error("Error creating egress: ", e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    public Mono<ResponseEntity<Egress>> updateEgress(String egressId, AdminEgressDto adminDto) {
        log.info("EDITANDO Egreso");

        return egressRepository.findById(egressId)
                .switchIfEmpty(Mono.error(new RuntimeException("Egress not found")))
                .flatMap(egress -> updateEgressDetails(egress, adminDto)
                        .flatMap(updatedEgress -> sendNotificationAndUpdateStatus(updatedEgress, adminDto)));
    }

    private Egress buildEgressFromDto(UserEgress userDto) {
        Egress egress = new Egress();
        egress.setEgressId(UUID.randomUUID().toString());
        egress.setPersonId(userDto.getPersonId());
        egress.setCategoryId(userDto.getCategoryId());
        egress.setType('E');
        egress.setFileUrls(List.of());
        egress.setStatusPayment(ACCEPT);
        egress.setStatusNotification(false);
        egress.setCreatedAt(LocalDateTime.now());
        egress.setUpdatedAt(LocalDateTime.now());
        return egress;
    }


    private Mono<Egress> updateEgressDetails(Egress egress, AdminEgressDto adminDto) {
        egress.setComment(adminDto.getComment());
        egress.setStatusPayment(adminDto.getStatusPayment());
        egress.setUserConfirmedId(adminDto.getUserConfirmedId());
        egress.setUpdatedAt(LocalDateTime.now());
        return egressRepository.save(egress);
    }

    private Mono<ResponseEntity<Egress>> sendNotificationAndUpdateStatus(Egress egress, AdminEgressDto adminDto) {
        return retrieveUsers(egress, adminDto)
                .flatMap(tuple -> {
                    User person = tuple.getT1();
                    User personConfirmed = tuple.getT2();

                    return buildNotificationEmail(egress, person, personConfirmed)
                            .flatMap(notificationEmail -> notificationWebClient.sendNotification(notificationEmail))
                            .flatMap(notificationResponse -> {
                                egress.setStatusNotification(notificationResponse.isSuccess());
                                return egressRepository.save(egress)
                                        .map(updatedEgress -> new ResponseEntity<>(updatedEgress, HttpStatus.OK));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error updating egress: ", e);
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    private Mono<Tuple2<User, User>> retrieveUsersList(Egress egress) {
        User emptyUser = new User();

        Mono<User> personMono = egress.getPersonId() != null ? userWebClient.getUserById(egress.getPersonId())
                : Mono.just(emptyUser);

        Mono<User> personConfirmedMono = egress.getUserConfirmedId() != null
                ? userWebClient.getUserById(egress.getUserConfirmedId())
                : Mono.just(emptyUser);

        return Mono.zip(personMono, personConfirmedMono);
    }

    private Mono<Tuple2<User, User>> retrieveUsers(Egress egress, AdminEgressDto adminDto) {
        Mono<User> personMono = userWebClient.getUserById(egress.getPersonId());
        Mono<User> personConfirmedMono = adminDto.getUserConfirmedId() != null
                ? userWebClient.getUserById(adminDto.getUserConfirmedId())
                : Mono.empty();
        return Mono.zip(personMono, personConfirmedMono);
    }

    private Mono<NotificationEmail> buildNotificationEmail(Egress egress, User person, User personConfirmed) {
        NotificationEmail notificationEmail = new NotificationEmail();
        notificationEmail.setCorrelative(egress.getEgressId());
        notificationEmail.setEmailUser(person != null ? person.getEmail() : "Unknown");
        notificationEmail.setUserName(person != null ? person.getFirstName() + " " + person.getLastName() : "Unknown");
        notificationEmail.setAdminName(personConfirmed != null
                ? personConfirmed.getFirstName() + " " + personConfirmed.getLastName()
                : "Unknown");
        notificationEmail.setComment(egress.getComment());
        notificationEmail.setStatusPayment(String.valueOf(egress.getStatusPayment()));

        return categoryRepository.findById(egress.getCategoryId())
                .map(category -> {
                    List<CategoryNotification> categoryNotifications = new ArrayList<>();
                    if (category != null) {
                        CategoryNotification categoryNotification = new CategoryNotification();
                        categoryNotification.setName(category.getName());
                        categoryNotification.setAmount(category.getAmount());
                        categoryNotifications.add(categoryNotification);
                    }
                    notificationEmail.setCategories(categoryNotifications);
                    return notificationEmail;
                })
                .defaultIfEmpty(notificationEmail);
    }

}
