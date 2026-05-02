package ru.yandex.practicum.catsgram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.catsgram.dal.ImageRepository;
import ru.yandex.practicum.catsgram.dto.ImageDto;
import ru.yandex.practicum.catsgram.dto.PostDto;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.ImageFileException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.mapper.ImageMapper;
import ru.yandex.practicum.catsgram.model.Image;
import ru.yandex.practicum.catsgram.model.ImageData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ImageService {
    private final PostService postService;
    // Укажите директорию для хранения изображений
    private final String imageDirectory = "C:\\ideaImg";
    private final ImageRepository imageRepository;

    // загружаем данные указанного изображения с диска
    public ImageData getImageData(long imageId) {
        Image image = imageRepository.findByImageId(imageId)
                .orElseThrow(()-> new NotFoundException("Изображение с id = " + imageId + " не найдено"));
        // загрузка файла с диска
        byte[] data = loadFile(image);

        return new ImageData(data, image.getOriginalFileName());
    }

    private byte[] loadFile(Image image) {
        Path path = Paths.get(image.getFilePath());
        if (Files.exists(path)) {
            try {
                return Files.readAllBytes(path);
            } catch (IOException e) {
                throw new ImageFileException("Ошибка чтения файла.  Id: " + image.getId()
                        + ", name: " + image.getOriginalFileName(), e);
            }
        } else {
            throw new ImageFileException("Файл не найден. Id: " + image.getId()
                    + ", name: " + image.getOriginalFileName());
        }
    }

    // получение данных об изображениях указанного поста
    public List<ImageDto> getPostImages(long postId) {
        return imageRepository.findByPostId(postId)
                .stream()
                .map(ImageMapper::mapToImageDto)
                .collect(Collectors.toList());
    }

    // сохранение списка изображений, связанных с указанным постом
    public List<ImageDto> saveImages(long postId, List<MultipartFile> files) {
        return files.stream().map(file -> saveImage(postId, file)).collect(Collectors.toList());
    }

    // сохранение отдельного изображения, связанного с указанным постом
    private ImageDto saveImage(long postId, MultipartFile file) {
        PostDto post = postService.getPostById(postId);
        if (post == null) {
               throw new ConditionsNotMetException("Указанный пост не найден");
        }
        // сохраняем изображение на диск и возвращаем путь к файлу
        Path filePath = saveFile(file, post);

        Image image = ImageMapper.mapToImage(post, file.getOriginalFilename(), filePath.toString());
        image = imageRepository.save(image);
        return ImageMapper.mapToImageDto(image);
    }

    // сохранение файла изображения
    private Path saveFile(MultipartFile file, PostDto post) {
        try {
            // формирование уникального названия файла на основе текущего времени и расширения оригинального файла
            String uniqueFileName = String.format("%d.%s", Instant.now().toEpochMilli(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename()));

            // формирование пути для сохранения файла с учётом идентификаторов автора и поста
            Path uploadPath = Paths.get(imageDirectory, String.valueOf(post.getAuthorId()), post.getId().toString());
            Path filePath = uploadPath.resolve(uniqueFileName);

            // создаём директории, если они ещё не созданы
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // сохраняем файл по сформированному пути
            file.transferTo(filePath);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}