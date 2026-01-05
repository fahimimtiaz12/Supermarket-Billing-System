package com.example.supermarketbillingsystem;

public class Cashier {
        private String username;
        private String password;
        private String status; // "offline" or "online"

        // Constructor, getters, and setters
        public Cashier(String username, String password, String status) {
            this.username = username;
            this.password = password;
            this.status = status;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }


}
