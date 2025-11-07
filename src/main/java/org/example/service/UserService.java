package org.example.service;


import org.example.model.User;
import org.example.repository.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    @Transactional
    @CacheEvict(value = {"users", "usersById", "allUsers"}, allEntries = true)
    public User registerNewUser(User user){
        if(userRepository.existsByUsername(user.getUsername())){
            throw new IllegalArgumentException("Uživatelské jméno již existuje");
        }
        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email již existuje");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(User.Role.ROLE_USER)); // Výchozí role

        User savedUser = userRepository.save(user);

        // Odeslání uvítacího emailu
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());

        return savedUser;
    }

    private void validateUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Registrace selhala - jméno {} již existuje", user.getUsername());
            throw new IllegalArgumentException("Uživatelské jméno již existuje");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registrace selhala - email {} již existuje", user.getEmail());
            throw new IllegalArgumentException("Email již existuje");
        }
    }

    // getters, setters, další metody...
    @CacheEvict(value = {"users", "usersById", "allUsers"}, allEntries = true)
    public void DeleteUserById(Long userId) {
        userRepository.deleteById(userId);
    }

    @Cacheable(value = "users", key = "#username")
    public Optional<User> findUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    @Cacheable(value = "usersById", key = "#userId")
    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }
    @Cacheable("allUsers")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }



}
