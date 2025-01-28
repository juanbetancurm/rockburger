package com.rockburger.arquetipo2024.domain.api;

import com.rockburger.arquetipo2024.domain.model.ClientModel;

public interface IClientServicePort {
    ClientModel createClient(ClientModel clientModel);
    boolean validateCredentials(String email, String password);
}