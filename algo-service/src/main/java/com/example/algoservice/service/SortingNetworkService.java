package com.example.algoservice.service;

import com.example.algoservice.dto.SortingNetworkExecuteRequestDto;
import com.example.algoservice.dto.SortingNetworkExecuteResponseDto;

public interface SortingNetworkService {
    SortingNetworkExecuteResponseDto execute(SortingNetworkExecuteRequestDto request);
}
