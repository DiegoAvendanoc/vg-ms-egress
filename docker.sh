#!/bin/bash

# Variables
DOCKER_USERNAME="diegoavendanio"
DOCKER_TOKEN="dckr_pat_uuBVtXDpUQ_LVx1bW5YSrvA1Th0"
DOCKER_IMAGE_NAME=""
DOCKER_IMAGE_TAG="latest"

# Función para mostrar el banner
function show_banner() {
    echo -e "\033[1;36m"
    echo "██████╗ ██╗██╗   ██╗██╗███╗   ██╗███████╗██╗  ██╗"
    echo "██╔══██╗██║██║   ██║██║████╗  ██║██╔════╝╚██╗██╔╝"
    echo "██║  ██║██║██║   ██║██║██╔██╗ ██║█████╗   ╚███╔╝ "
    echo "██║  ██║██║╚██╗ ██╔╝██║██║╚██╗██║██╔══╝   ██╔██╗ "
    echo "██████╔╝██║ ╚████╔╝ ██║██║ ╚████║███████╗██╔╝ ██╗"
    echo "╚═════╝ ╚═╝  ╚═══╝  ╚═╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝"
    echo -e "\033[0m"
    echo "Autor: Michael Joseph Quispe Chavez"
}

function docker_login() {
    echo "🔑 Iniciando sesión en Docker Hub..."
    echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USERNAME" --password-stdin
}

function show_main_menu() {
    show_banner
    echo -e "\nBienvenido a la consola de Docker. ¿Qué te gustaría hacer hoy?"
    echo "1) Crear imagen y subir a Docker"
    echo "2) Eliminar todos los contenedores e imágenes"
    echo "3) Bajar una imagen"
    echo "4) Salir"
    echo -n "Selecciona una opción: "
}

function handle_create_and_upload() {
    echo -n "🔧 Introduce el nombre de la imagen que deseas crear: "
    read image_name
    echo -n "🔧 Introduce el tag para la imagen (presiona Enter para usar 'latest'): "
    read image_tag
    image_tag=${image_tag:-latest}

    echo "🔧 Construyendo la imagen '$DOCKER_USERNAME/$image_name:$image_tag'..."
    docker build -t "$DOCKER_USERNAME/$image_name:$image_tag" .

    if [ $? -eq 0 ]; then
        echo "⬆ Subiendo la imagen '$DOCKER_USERNAME/$image_name:$image_tag' a Docker Hub..."
        docker push "$DOCKER_USERNAME/$image_name:$image_tag"

        if [ $? -eq 0 ]; then
            echo "✅ Imagen '$DOCKER_USERNAME/$image_name:$image_tag' subida exitosamente."
        else
            echo "❌ Error al subir la imagen."
        fi
    else
        echo "❌ Error al construir la imagen."
    fi
}

function handle_remove_all() {
    echo "🗑 Eliminando todos los contenedores..."
    docker container rm -f $(docker container ls -aq)

    echo "🗑 Eliminando todas las imágenes..."
    docker image rm -f $(docker image ls -aq)

    echo "✅ Todos los contenedores e imágenes han sido eliminados."
}

function handle_download() {
    echo -e "\n📜 Opciones de descarga:"
    echo "1) Bajar una imagen"
    echo "2) Volver al menú principal"
    echo -n "Selecciona una opción: "
    read download_option

    case $download_option in
    1)
        echo -e "\n🔽 ¿Qué tipo de imágenes quieres bajar?"
        echo "1) Imágenes personales"
        echo "2) Imágenes de otros usuarios"
        echo -n "Selecciona una opción: "
        read image_type

        case $image_type in
        1)
            echo -e "\n� Listando tus imágenes personales en Docker Hub..."
            page=1
            while true; do
                response=$(curl -s -H "Authorization: Bearer $DOCKER_TOKEN" \
                    "https://hub.docker.com/v2/repositories/$DOCKER_USERNAME/?page=$page&page_size=100")

                if [ -z "$response" ]; then
                    echo "❌ No se pudo obtener la lista de imágenes."
                    return
                fi

                images=$(echo "$response" | jq -r '.results[] | "\(.name)"')

                if [ -z "$images" ]; then
                    echo "✅ No hay más imágenes para mostrar."
                    break
                fi

                i=1
                while read -r image; do
                    printf "%2d) %-30s" "$i" "$image"
                    ((i++))
                    if ((i % 4 == 1)); then
                        echo
                    fi
                done <<<"$images"
                echo

                echo -n "🔍 Selecciona la imagen para bajar: "
                read selected_image_index
                selected_image=$(echo "$images" | sed -n "${selected_image_index}p")

                # Listar versiones (tags) de la imagen seleccionada
                echo -e "\n📜 Listando versiones de la imagen '$selected_image'..."
                tags_response=$(curl -s -H "Authorization: Bearer $DOCKER_TOKEN" \
                    "https://hub.docker.com/v2/repositories/$DOCKER_USERNAME/$selected_image/tags/?page_size=100")
                if [ -z "$tags_response" ]; then
                    echo "❌ No se pudo obtener la lista de versiones."
                    return
                fi

                tags=$(echo "$tags_response" | jq -r '.results[] | "\(.name)"')

                i=1
                while read -r tag; do
                    printf "%2d) %-20s" "$i" "$tag"
                    ((i++))
                    if ((i % 4 == 1)); then
                        echo
                    fi
                done <<<"$tags"
                echo

                echo -n "🔍 Selecciona la versión para la imagen '$selected_image': "
                read selected_tag_index
                selected_tag=$(echo "$tags" | sed -n "${selected_tag_index}p")
                echo -n "🔍 Introduce el tag para la imagen '$selected_image' (presiona Enter para usar '$selected_tag'): "
                read tag
                tag=${tag:-$selected_tag}
                echo -e "\n⬇ Descargando la imagen '$DOCKER_USERNAME/$selected_image:$tag'..."

                docker pull "$DOCKER_USERNAME/$selected_image:$tag"
                if [ $? -eq 0 ]; then
                    echo "✅ Imagen '$DOCKER_USERNAME/$selected_image:$tag' descargada exitosamente."
                else
                    echo "❌ Error al descargar la imagen."
                fi

                page=$((page + 1))
            done
            ;;
        2)
            echo -n "🔍 Introduce el nombre de la imagen para bajar (ejemplo: nombre-imagen): "
            read other_image
            echo -n "🔍 Introduce el tag para la imagen '$other_image' (presiona Enter para usar 'latest'): "
            read tag
            tag=${tag:-latest}
            echo -e "\n⬇ Descargando la imagen '$other_image:$tag'..."

            docker pull "$other_image:$tag"
            if [ $? -eq 0 ]; then
                echo "✅ Imagen '$other_image:$tag' descargada exitosamente."
            else
                echo "❌ Error al descargar la imagen."
            fi
            ;;
        *)
            echo "❌ Opción no válida."
            ;;
        esac
        ;;
    2)
        return
        ;;
    *)
        echo "❌ Opción no válida."
        ;;
    esac
}

docker_login
while true; do
    show_main_menu
    read option

    case $option in
    1)
        handle_create_and_upload
        ;;
    2)
        handle_remove_all
        ;;
    3)
        handle_download
        ;;
    4)
        echo "👋 Salida. ¡Hasta luego!"
        exit 0
        ;;
    *)
        echo "❌ Opción no válida."
        ;;
    esac
done