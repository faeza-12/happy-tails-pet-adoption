package com.petadoption;

import com.petadoption.server.PetAdoptionServer;

public class Main {

    public static void main(String[] args) {

        try {

            PetAdoptionServer server =
                    new PetAdoptionServer();

            server.start();

        } catch (Exception e) {

            System.out.println(
                    "Could not start the server."
            );

            e.printStackTrace();
        }
    }
}