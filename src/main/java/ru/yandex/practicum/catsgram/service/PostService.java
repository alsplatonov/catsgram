package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.exception.ParameterNotValidException;
import ru.yandex.practicum.catsgram.model.Post;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Указываем, что класс PostService - является бином и его
// нужно добавить в контекст приложения
@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

//    public Collection<Post> findAll() {
//        return posts.values();
//    }

    public Collection<Post> findAll(int size, String sort, int from) {
        SortOrder sortOrder = SortOrder.from(sort);

        if (sortOrder == null) {
            throw new ParameterNotValidException("sort", "Получено: " + sort + " должно быть: ask или desc");
        }
        if (size <= 0) {
            throw new ParameterNotValidException("size", "Размер должен быть больше нуля");
        }

        if (from < 0) {
            throw new ParameterNotValidException("from", "Начало выборки должно быть положительным числом");
        }

        return posts.values().stream()
                // сортировка по дате
                .sorted((p1, p2) -> {
                    if (sortOrder == SortOrder.ASCENDING) {
                        return p1.getPostDate().compareTo(p2.getPostDate());
                    } else {
                        return p2.getPostDate().compareTo(p1.getPostDate());
                    }
                })
                // пропускаем from
                .skip(from)
                // берем size
                .limit(size)
                .toList();
    }

    public Optional<Post> findById(long postId) {
        return Optional.ofNullable(posts.get(postId));
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        //ПРОВЕРКА АВТОРА
        long authorId = post.getAuthorId();
        if (userService.findUserById(authorId).isEmpty()) {
            throw new ConditionsNotMetException(
                    "Автор с id = " + authorId + " не найден"
            );
        }

        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public enum SortOrder {
        ASCENDING, DESCENDING;

        // Преобразует строку в элемент перечисления
        public static SortOrder from(String order) {
            switch (order.toLowerCase()) {
                case "ascending":
                case "asc":
                    return ASCENDING;
                case "descending":
                case "desc":
                    return DESCENDING;
                default: return null;
            }
        }
    }

}