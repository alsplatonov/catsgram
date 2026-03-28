package ru.yandex.practicum.catsgram.exception;

import java.io.IOException;

public class ImageFileException extends RuntimeException {
    public ImageFileException(String s, IOException e) {
    }
    public ImageFileException(String message) {
        super(message);
    }
}
