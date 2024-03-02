package com.example.photoapp.service.impl;


import com.example.photoapp.entities.*;
import com.example.photoapp.entities.dto.FriendRequestDto;
import com.example.photoapp.entities.dto.LoginUserDto;
import com.example.photoapp.entities.dto.UserDto;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import com.example.photoapp.enums.RegistrationStatusEnum;
import com.example.photoapp.repositories.FriendRequestRepository;
import com.example.photoapp.repositories.RoleRepository;
import com.example.photoapp.repositories.UserConfirmationRepository;
import com.example.photoapp.repositories.UserRepository;
import com.example.photoapp.util.TbConstants;
import com.example.photoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserConfirmationRepository userConfirmationRepository;

    @Autowired
    private EmailServiceImpl emailService;
    public User currentUser;

    @Override
    public User login(LoginUserDto loginUser) {
        currentUser = userRepository.findByEmail(loginUser.getEmail());
        if (Objects.nonNull(currentUser)) {
            return passwordEncoder.matches(loginUser.getPassword(), currentUser.getPassword()) ?
                    currentUser : null;
        } else {
            return null;
        }
    }

    @Override
    public void logout(LoginUserDto loginUser) {
        currentUser = null;
    }

    @Override
    public void saveUser(UserDto userDto) {
        Role role = roleRepository.findByName(TbConstants.Roles.USER);
        List<User> friends = new ArrayList<>();
        List<Photo> photos = new ArrayList<>();

        if (role == null)
            role = roleRepository.save(new Role(TbConstants.Roles.USER));

        User user = new User(userDto.getName(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()),
                List.of(role), photos, friends, RegistrationStatusEnum.PENDING);
        Random random = new Random();
        String randomNumber = String.valueOf(100000 + random.nextInt(900000));
        UserConfirmation userConfirmation = new UserConfirmation();
        userConfirmation.setEmail(userDto.getEmail());
        userConfirmation.setConfirmationCode(randomNumber);
        userConfirmationRepository.save(userConfirmation);
        userRepository.save(user);

        emailService.sendEmail(userDto.getEmail(),"code",randomNumber);
    }

    public void addFriend(String name) {
        saveFriendRequest(name);
//        User foundUser = userRepository.findByName(name);
//        User currentUser = customUserDetailsService.currentUser;
//        currentUser.getFriends().add(foundUser);
//        userRepository.save(currentUser);
    }

    public void confirmFriendRequest(Long friendRequestId) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(friendRequestId);
        if (friendRequest.isPresent()) {
            User sender = userRepository.findById(friendRequest.get().getSenderId()).get();
            User receiver = userRepository.findById(friendRequest.get().getReceiverId()).get();
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            friendRequest.get().setStatus(FriendRequestStatusEnum.ACCEPTED.toString());
            userRepository.save(sender);
            userRepository.save(receiver);
            friendRequestRepository.save(friendRequest.get());
        }
    }

    public void rejectFriendRequest(Long friendRequestId) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(friendRequestId);
        if (friendRequest.isPresent()) {
            friendRequest.get().setStatus(FriendRequestStatusEnum.REJECTED.toString());
            friendRequestRepository.save(friendRequest.get());
        }
    }

    public void saveFriendRequest(String receiverName) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(currentUser.getId());
        friendRequest.setReceiverId(userRepository.findByName(receiverName).getId());
        friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequestDto> getFriendRequests(Long receiverId) {
        List<FriendRequest> foundFriendRequest = friendRequestRepository.findFriendRequestByReceiverIdAndStatus(receiverId, FriendRequestStatusEnum.PENDING.toString());
        List<FriendRequestDto> resultList = new ArrayList<>();
        for (FriendRequest friendRequest : foundFriendRequest) {
            String senderName = userRepository.findById(friendRequest.getSenderId()).get().getName();// getSender.getName
            resultList.add(new FriendRequestDto(friendRequest.getId(), senderName));
        }
        return resultList;
    }

    public void removeFriend(String userNameForRemoval) {

        List<User> friendsList = currentUser.getFriends();
        friendsList.stream()
                .filter(friend -> friend.getName().equals(userNameForRemoval))
                .findFirst()
                .ifPresent(friendsList::remove);

        User userForRemoval = userRepository.findByName(userNameForRemoval);
        List<User> userForRemovalFriendList = userForRemoval.getFriends();
        userForRemovalFriendList.stream()
                .filter(friend -> friend.getName().equals(currentUser.getName()))
                .findFirst()
                .ifPresent(userForRemovalFriendList::remove);

        userRepository.save(currentUser);
        userRepository.save(userForRemoval);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Boolean confirmUser(String email, String confirmCode){
        UserConfirmation userConfirmation = userConfirmationRepository.findByEmail(email);
        if(userConfirmation != null){
            return userConfirmation.getConfirmationCode().equals(confirmCode);
        }
        return false;
    }

    @Override
    public void updateRegistrationStatus(String email, RegistrationStatusEnum registrationStatusEnum){
        User user = userRepository.findByEmail(email);
        user.setRegistrationStatus(registrationStatusEnum);
        userRepository.save(user);
    }

}
