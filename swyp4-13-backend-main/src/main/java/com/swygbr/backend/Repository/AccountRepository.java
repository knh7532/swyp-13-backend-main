package com.swygbr.backend.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.swygbr.backend.model.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);
}
