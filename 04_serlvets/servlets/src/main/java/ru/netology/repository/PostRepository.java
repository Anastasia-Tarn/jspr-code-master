package ru.netology.repository;

import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Stub
public class PostRepository {

  private AtomicLong count;

  private final ConcurrentHashMap<Long, Post> posts;
  public PostRepository(ConcurrentHashMap<Long, Post> posts) {
    this.posts = posts;
  }
  public List<Post> all() {
    return Collections.emptyList();
  }

  public Optional<Post> getById(long id) {
    return Optional.empty();
  }

  public Post save(Post post) {

    if(post.getId() == 0) {
      var newId = count.getAndIncrement();
      post.setId(newId);
      posts.put(post.getId(), post);
      count = new AtomicLong(newId);
    } else if (post.getId() != 0 && !posts.containsKey(post.getId())) {
      throw new NotFoundException();
    } else {
      posts.put(post.getId(), post);
      return post;

    }return post;
  }

  public void removeById(long id) {
  }
}
