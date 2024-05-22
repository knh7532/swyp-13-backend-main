package com.swygbr.backend.login.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.swygbr.backend.login.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);
}
