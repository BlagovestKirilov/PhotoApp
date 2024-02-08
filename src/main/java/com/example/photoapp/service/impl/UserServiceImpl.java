package com.example.photoapp.service.impl;



import com.example.photoapp.entities.FriendRequest;
import com.example.photoapp.entities.Photo;
import com.example.photoapp.entities.dto.FriendRequestDto;
import com.example.photoapp.entities.dto.UserDto;
import com.example.photoapp.entities.Role;
import com.example.photoapp.entities.User;
import com.example.photoapp.enums.FriendRequestStatusEnum;
import com.example.photoapp.repositories.FriendRequestRepository;
import com.example.photoapp.repositories.RoleRepository;
import com.example.photoapp.repositories.UserRepository;
import com.example.photoapp.util.TbConstants;
import com.example.photoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;

    @Autowired private RoleRepository roleRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private CustomUserDetailsService customUserDetailsService;

    @Autowired private FriendRequestRepository friendRequestRepository;
    @Override
    public void saveUser(UserDto userDto) {
        Role role = roleRepository.findByName(TbConstants.Roles.USER);
        List<User> friends = new ArrayList<>();
        List<Photo> photos = new ArrayList<>();

        if (role == null)
            role = roleRepository.save(new Role(TbConstants.Roles.USER));

        User user = new User(userDto.getName(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()),
                List.of(role),photos,friends);
        userRepository.save(user);

    }
    public void addFriend(String name){
        saveFriendRequest(name);
//        User foundUser = userRepository.findByName(name);
//        User currentUser = customUserDetailsService.currentUser;
//        currentUser.getFriends().add(foundUser);
//        userRepository.save(currentUser);
    }
    public void confirmFriendRequest(Long friendRequestId){
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(friendRequestId);
        if(friendRequest.isPresent()) {
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

    public void rejectFriendRequest(Long friendRequestId){
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(friendRequestId);
        if(friendRequest.isPresent()){
            friendRequest.get().setStatus(FriendRequestStatusEnum.REJECTED.toString());
            friendRequestRepository.save(friendRequest.get());
        }
    }

    public void saveFriendRequest(String receiverName){
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(customUserDetailsService.currentUser.getId());
        friendRequest.setReceiverId(userRepository.findByName(receiverName).getId());
        friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequestDto> getFriendRequests(Long receiverId){
        List<FriendRequest> foundFriendRequest = friendRequestRepository.findFriendRequestByReceiverIdAndStatus(receiverId, FriendRequestStatusEnum.PENDING.toString());
        List<FriendRequestDto> resultList = new ArrayList<>();
        for(FriendRequest friendRequest: foundFriendRequest){
            String senderName = userRepository.findById(friendRequest.getSenderId()).get().getName();// getSender.getName
            resultList.add(new FriendRequestDto(friendRequest.getId(),senderName));
        }
        return resultList;
    }

    public void removeFriend(String userNameForRemoval) {
        User currentUser = customUserDetailsService.currentUser;

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
}
