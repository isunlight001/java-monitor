package com.acme.monitor.cache;

import java.io.Serializable;

public class TestUser implements Serializable {
    private Long id;
    private String name;
    private String email;
    
    public TestUser() {}
    
    public TestUser(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TestUser testUser = (TestUser) o;
        
        if (id != null ? !id.equals(testUser.id) : testUser.id != null) return false;
        if (name != null ? !name.equals(testUser.name) : testUser.name != null) return false;
        return email != null ? email.equals(testUser.email) : testUser.email == null;
    }
    
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }
}