package com.example.daj2ee.service;

import com.example.daj2ee.dto.request.RedeemRequest;
import com.example.daj2ee.dto.request.UpdateUserProfileRequest;
import com.example.daj2ee.dto.response.RedeemResponse;
import com.example.daj2ee.dto.response.UserDto;
import com.example.daj2ee.entity.User;
import com.example.daj2ee.repository.UserRepository;
import com.example.daj2ee.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public RedeemResponse redeemPoints(UserPrincipal principal, RedeemRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getPoints() < request.points()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient points to redeem");
        }

        user.setPoints(user.getPoints() - request.points());
        userRepository.save(user);

        return new RedeemResponse(
                request.item(),
                request.points(),
                user.getPoints(),
                "Successfully redeemed " + request.points() + " points for " + request.item()
        );
    }

    @Override
    @Transactional
    public UserDto updateProfile(UserPrincipal principal, UpdateUserProfileRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Update profile request is required");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFullName(trimToNull(request.fullName()));
        user.setPhoneNumber(trimToNull(request.phoneNumber()));
        user.setLocation(trimToNull(request.location()));
        user.setBio(trimToNull(request.bio()));
        user.setAvatarUrl(trimToNull(request.avatarUrl()));

        User updatedUser = userRepository.save(user);
        return UserDto.fromEntity(updatedUser);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
