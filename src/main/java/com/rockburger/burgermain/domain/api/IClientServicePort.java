package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.ClientModel;

public interface IClientServicePort {
    ClientModel createClient(ClientModel clientModel);
    boolean validateCredentials(String email, String password);
}