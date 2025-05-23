package com.example.travel.service;

import com.example.travel.entity.User;

public interface IUserService {
    User findByUsername(String username);
}