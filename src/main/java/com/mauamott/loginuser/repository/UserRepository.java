package com.mauamott.loginuser.repository;

import com.mauamott.loginuser.documents.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
}
