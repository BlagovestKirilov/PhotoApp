package com.example.photoapp.service.impl;


import com.example.photoapp.entities.*;
import com.example.photoapp.entities.dto.*;
import com.example.photoapp.enums.*;
import com.example.photoapp.repositories.*;
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
    private PhotoRepository photoRepository;

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
            currentUser.setFriendList(userRepository.findFriendsByEmail(currentUser.getEmail()));
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
        List<Photo> photos = new ArrayList<>();
        String DEFAULT_PROFILE_PICTURE = "default_profile_picture.jpg";

        if (role == null)
            role = roleRepository.save(new Role(TbConstants.Roles.USER));

        User user = new User(userDto.getName(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()),
                List.of(role), photos, new ArrayList<>(), RegistrationStatusEnum.PENDING, photoRepository.findByFileNameEndsWith(DEFAULT_PROFILE_PICTURE));
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

    public void confirmFriendRequest(String senderEmail) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findFirstBySenderAndReceiverAndStatus(
                userRepository.findByEmail(senderEmail),
                currentUser,
                FriendRequestStatusEnum.PENDING
        );
        if (friendRequest.isPresent()) {
            User sender = friendRequest.get().getSender();
            sender.getFriendList().add(currentUser);
            currentUser.getFriendList().add(sender);
            friendRequest.get().setStatus(FriendRequestStatusEnum.ACCEPTED);
            userRepository.save(sender);
            userRepository.save(currentUser);
            friendRequestRepository.save(friendRequest.get());
        }
    }

    public void rejectFriendRequest(String senderEmail) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findFirstBySenderAndReceiverAndStatus(
                userRepository.findByEmail(senderEmail),
                currentUser,
                FriendRequestStatusEnum.PENDING
        );
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
        List<FriendRequest> foundFriendRequest = friendRequestRepository.findAllByReceiverAndStatus(currentUser, FriendRequestStatusEnum.PENDING);
        List<FriendRequestDto> resultList = new ArrayList<>();
        for (FriendRequest friendRequest : foundFriendRequest) {
            String senderName = friendRequest.getSender().getName();
            resultList.add(new FriendRequestDto(friendRequest.getId(), senderName));
        }
        return resultList;
    }

    public void removeFriend(String removeUserEmail) {

        List<User> friendsList = currentUser.getFriendList();
        friendsList.stream()
                .filter(friend -> friend.getEmail().equals(removeUserEmail))
                .findFirst()
                .ifPresent(friendsList::remove);

        User userForRemoval = userRepository.findByEmail(removeUserEmail);
        List<User> userForRemovalFriendList = userForRemoval.getFriendList();
        userForRemovalFriendList.stream()
                .filter(friend -> friend.getEmail().equals(currentUser.getEmail()))
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

    private String getRandomNumber(){
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
