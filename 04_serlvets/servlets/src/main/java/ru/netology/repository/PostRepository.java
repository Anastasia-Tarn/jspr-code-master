package ru.netology.repository;

import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Stub
public interface PostRepository {

  List<Post> all();

  Optional<Post> getById(long id);

  Post save(Post post);

  void removeById(long id);
}
