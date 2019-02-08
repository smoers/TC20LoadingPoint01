package com.aerospace.sabena.tc20.loadingpoint.models;

/**
 * Classe representant un utilisateur de l'application
 */
public class User {
    private String roleNumber;
    private String lastName;
    private String firstName;

    /**
     * Constructeur
     * @param roleNumber
     */
    public User(String roleNumber) {
        this.roleNumber = roleNumber;
    }

    public String getRoleNumber() {
        return roleNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
