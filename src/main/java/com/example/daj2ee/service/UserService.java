package com.example.daj2ee.service;

import com.example.daj2ee.dto.request.RedeemRequest;
import com.example.daj2ee.dto.request.UpdateUserProfileRequest;
import com.example.daj2ee.dto.response.RedeemResponse;
import com.example.daj2ee.dto.response.UserDto;
import com.example.daj2ee.security.UserPrincipal;

public interface UserService {
    RedeemResponse redeemPoints(UserPrincipal principal, RedeemRequest request);

    UserDto updateProfile(UserPrincipal principal, UpdateUserProfileRequest request);
}
