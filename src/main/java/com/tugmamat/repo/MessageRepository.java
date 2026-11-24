package com.tugmamat.repo;

import com.tugmamat.model.Message;
import com.tugmamat.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderOrRecipientOrderByCreatedAtDesc(User sender, User recipient);
}
