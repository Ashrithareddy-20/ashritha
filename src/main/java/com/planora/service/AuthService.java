package com.planora.service;

import com.planora.dto.LoginRequestDto;
import com.planora.dto.LoginResponseDto;
import com.planora.dto.RegisterRequestDto;
import com.planora.dto.RegisterResponseDto;
import com.planora.security.UserPrincipal;

public interface AuthService {

    RegisterResponseDto register(RegisterRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto loginRequestDto);

    RegisterResponseDto getCurrentUser(UserPrincipal principal);
}
