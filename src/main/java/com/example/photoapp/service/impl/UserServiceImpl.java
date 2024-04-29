package com.example.photoapp.service.impl;


import com.example.photoapp.entities.*;
import com.example.photoapp.entities.dto.*;
import com.example.photoapp.enums.*;
import com.example.photoapp.repositories.FriendRequestRepository;
import com.example.photoapp.repositories.RoleRepository;
import com.example.photoapp.repositories.UserConfirmationRepository;
import com.example.photoapp.repositories.UserRepository;
import com.example.photoapp.util.TbConstants;
import com.example.photoapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

//    @Autowired
//    private PhotoService photoService;

    @Autowired
    private Random random;

    public User currentUser;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User login(LoginUserDto loginUser) {
        currentUser = userRepository.findByEmail(loginUser.getEmail());
        if (Objects.nonNull(currentUser) && passwordEncoder.matches(loginUser.getPassword(), currentUser.getPassword())) {
            LOGGER.info("User with email: " + currentUser.getEmail() + " logged in.");
            return currentUser;
        } else {
            return null;
        }
    }

    @Override
    public void logout() {
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
        String randomNumber = getRandomNumber();
        UserConfirmation userConfirmation = new UserConfirmation();
        userConfirmation.setEmail(userDto.getEmail());
        userConfirmation.setConfirmationCode(passwordEncoder.encode(randomNumber));
        userConfirmation.setCodeConfirmation(CodeConfirmationEnum.REGISTRATION);
        userConfirmation.setCodeConfirmationStatus(CodeConfirmationStatusEnum.ACTIVE);
        userConfirmationRepository.save(userConfirmation);
        userRepository.save(user);
        LOGGER.info(randomNumber);
        emailService.sendEmail(userDto.getEmail(),"code",randomNumber);
    }

    public void addFriend(String receiverEmail) {
        saveFriendRequest(receiverEmail);
//        User foundUser = userRepository.findByName(name);
//        User currentUser = customUserDetailsService.currentUser;
//        currentUser.getFriends().add(foundUser);
//        userRepository.save(currentUser);
    }

    public void confirmFriendRequest(Long friendRequestId) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(friendRequestId);
        if (friendRequest.isPresent()) {
            User sender = friendRequest.get().getSender();
            User receiver = friendRequest.get().getReceiver();
            sender.getFriends().add(receiver);
            receiver.getFriends().add(sender);
            friendRequest.get().setStatus(FriendRequestStatusEnum.ACCEPTED);
            userRepository.save(sender);
            userRepository.save(receiver);
            friendRequestRepository.save(friendRequest.get());
        }
    }

    public void rejectFriendRequest(Long friendRequestId) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(friendRequestId);
        if (friendRequest.isPresent()) {
            friendRequest.get().setStatus(FriendRequestStatusEnum.REJECTED);
            friendRequestRepository.save(friendRequest.get());
        }
    }

    public void saveFriendRequest(String receiverEmail) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(currentUser);
        friendRequest.setReceiver(userRepository.findByEmail(receiverEmail));
        friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequestDto> getFriendRequests(Long receiverId) {
        List<FriendRequest> foundFriendRequest = friendRequestRepository.findFriendRequestByReceiverIdAndStatus(receiverId, FriendRequestStatusEnum.PENDING);
        List<FriendRequestDto> resultList = new ArrayList<>();
        for (FriendRequest friendRequest : foundFriendRequest) {
            String senderName = friendRequest.getSender().getName();
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
    public Boolean confirmUser(ConfirmationDto confirmationDto) {
        UserConfirmation userConfirmation = userConfirmationRepository
                .findUserConfirmationByEmailAndCodeConfirmationAndCodeConfirmationStatus(confirmationDto.getEmail(), confirmationDto.getCodeConfirmationEnum(), CodeConfirmationStatusEnum.ACTIVE);
        if (userConfirmation != null) {
            if (passwordEncoder.matches(confirmationDto.getConfirmationCode(), userConfirmation.getConfirmationCode())) {
                userConfirmation.setCodeConfirmationStatus(CodeConfirmationStatusEnum.EXPIRED);
                userConfirmationRepository.save(userConfirmation);
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateRegistrationStatus(String email, RegistrationStatusEnum registrationStatusEnum){
        User user = userRepository.findByEmail(email);
        user.setRegistrationStatus(registrationStatusEnum);
        userRepository.save(user);
    }

    public void changePassword(ChangePasswordDto changePasswordDto){
        User changeUser = userRepository.findByEmail(changePasswordDto.getEmail());
        if(changePasswordDto.getChangePasswordEnum() == ChangePasswordEnum.CHANGE_PASSWORD) {
            if (Objects.nonNull(currentUser) && passwordEncoder.matches(changePasswordDto.getOldPassword(), changeUser.getPassword())) {
                changeUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
                userRepository.save(changeUser);
                //return true;
            }
        } else if (changePasswordDto.getChangePasswordEnum() == ChangePasswordEnum.FORGOT_PASSWORD) {
            changeUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
            userRepository.save(changeUser);
        }
        //return false;
    }

    public void forgotPassword(String email){
        String randomNumber = getRandomNumber();
        UserConfirmation userConfirmation = new UserConfirmation();
        userConfirmation.setEmail(email);
        userConfirmation.setConfirmationCode(passwordEncoder.encode(randomNumber));
        userConfirmation.setCodeConfirmation(CodeConfirmationEnum.CHANGE_PASSWORD);
        userConfirmation.setCodeConfirmationStatus(CodeConfirmationStatusEnum.ACTIVE);
        userConfirmationRepository.save(userConfirmation);

        emailService.sendEmail(email,"Forgot Registration",randomNumber);
    }

//    public List<AddFriendDto> findNonFriendUsers(){
//        List<User> users = userRepository.findNonFriendUsersByEmail(currentUser.getEmail());
//        return users.stream()
//                .map(user -> {
//                    AddFriendDto addFriendDto = new AddFriendDto();
//                    addFriendDto.setName(user.getName());
//                    ResponseEntity<byte[]> b = photoService.getImage(user.getProfilePhoto().getFileName());
//                    addFriendDto.setProfilePhotoData(Base64.getEncoder().encodeToString(b.getBody()));
//                    return addFriendDto;
//                })
//                .collect(Collectors.toList());
//    }
    private String getRandomNumber(){
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
