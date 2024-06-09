package com.example.photoapp.service.impl;


import com.example.photoapp.entity.*;
import com.example.photoapp.entity.dto.*;
import com.example.photoapp.enums.*;
import com.example.photoapp.repository.*;
import com.example.photoapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    private NotificationRepository notificationRepository;

    @Autowired
    private UserBanRepository userBanRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private Random random;

    @Autowired
    private PageRepository pageRepository;

    public User currentUser;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final String DEFAULT_PROFILE_PICTURE = "default_profile_picture.jpg";

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
        String randomNumber = getRandomNumber();
        if (Objects.isNull(userRepository.findByEmail(userDto.getEmail()))) {
            Role role = roleRepository.findByName(RoleEnum.ROLE_USER);

            User user = new User(userDto.getName(), userDto.getEmail(), userDto.getCountry(), passwordEncoder.encode(userDto.getPassword()),
                    role, new ArrayList<>(), RegistrationStatusEnum.PENDING, photoRepository.findByFileNameEndsWith(DEFAULT_PROFILE_PICTURE));
            UserConfirmation userConfirmation = new UserConfirmation();
            userConfirmation.setEmail(userDto.getEmail());
            userConfirmation.setConfirmationCode(passwordEncoder.encode(randomNumber));
            userConfirmation.setCodeConfirmation(CodeConfirmationEnum.REGISTRATION);
            userConfirmation.setCodeConfirmationStatus(CodeConfirmationStatusEnum.ACTIVE);
            userConfirmationRepository.save(userConfirmation);
            userRepository.save(user);
        }
        LOGGER.info(randomNumber);
        emailService.sendEmail(userDto.getEmail(), "code", randomNumber);
    }

    public void addFriend(String receiverEmail) {
        User receiver = userRepository.findByEmail(receiverEmail);
        saveFriendRequest(receiver);
        String notificationText = currentUser.getName() + " sent you friend request.";
        generateNotification(receiver, notificationText);
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

    public void saveFriendRequest(User receiver) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(currentUser);
        friendRequest.setReceiver(receiver);
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
    public User findUserByEmailForRegistration(String email) {
        User user = userRepository.findByEmail(email);
        if(Objects.nonNull(user) && user.getRegistrationStatus().equals(RegistrationStatusEnum.CONFIRMED)){
            return user;
        } else {
            return null;
        }
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

    @Override
    public void banUser(UserBanDto userBanDto){
        UserBan userBan = new UserBan();
        Photo photo = photoRepository.findByFileName(userBanDto.getPhotoFileName());
        User user = photo.getUser();
        if(user == null){
            user = photo.getPage().getOwner();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        userBan.setUser(user);
        userBan.setAdminUser(currentUser);
        userBan.setReason(ReportReasonEnum.valueOf(userBanDto.getReason()));
        userBan.setStartDate(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, userBanDto.getBanDuration());
        userBan.setEndDate(calendar.getTime());
        userBanRepository.save(userBan);
        generateNotification(user, String.format("You received a ban for uploading photos for %s days!", userBanDto.getBanDuration()));
    }

    public void generateNotification(User receiver, String text){
        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setMessage(text);
        notificationRepository.save(notification);
    }

    public List<String> getCurrentUserNotification(){
        return notificationRepository.findAllByUserOrderByDateDesc(currentUser)
                .stream()
                .map(Notification::getMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void editProfile(UserDto userDto) {
        if(!currentUser.getName().equals(userDto.getName())){
            currentUser.setName(userDto.getName());
        }
        if(!currentUser.getEmail().equals(userDto.getEmail())){
            currentUser.setEmail(userDto.getEmail());
        }
        if(!currentUser.getCountry().equals(userDto.getCountry())){
            currentUser.setCountry(userDto.getCountry());
        }
        if(currentUser.getEducation() == null || !currentUser.getEducation().equals(userDto.getEducation())){
            currentUser.setEducation(userDto.getEducation());
        }
        if(currentUser.getBirtdate() == null || !currentUser.getBirtdate().equals(userDto.getBirtdate())){
            currentUser.setBirtdate(userDto.getBirtdate());
        }
        userRepository.save(currentUser);
    }

    public void editPage(PageDto pageDto) {
        Page page = pageRepository.findAllByPageName(pageDto.getName());
        if (page.getPageName() != null && !page.getPageName().equals(pageDto.getNewPageName())) {
            page.setPageName(pageDto.getNewPageName());
        }

        if (page.getDescription() == null || (pageDto.getDescription() != null && !page.getDescription().equals(pageDto.getDescription()))) {
            page.setDescription(pageDto.getDescription());
        }

        if (page.getWebsite() == null || (pageDto.getWebsite() != null && !page.getWebsite().equals(pageDto.getWebsite()))) {
            page.setWebsite(pageDto.getWebsite());
        }
        pageRepository.save(page);
    }
    public Boolean isThereActiveBans(){
        return !userBanRepository.findAllByUser(currentUser).stream().filter(userBan -> userBan.getEndDate().after(new Date())).toList().isEmpty();
    }

    public void savePage(String pageName){
        Page page = new Page();
        page.setPageName(pageName);
        page.setOwner(currentUser);
        page.setProfilePhoto(photoRepository.findByFileNameEndsWith(DEFAULT_PROFILE_PICTURE));
        pageRepository.save(page);
    }

    public void likePage(String pageName, String userEmail){
        Page page = pageRepository.findAllByPageName(pageName);
        User user = userRepository.findByEmail(userEmail);
        page.getLikedPageUsers().add(user);
        generateNotification(page.getOwner(), user.getName() + " liked your page!");
        pageRepository.save(page);
    }

    public void unlikePage(String pageName, String userEmail){
        Page page = pageRepository.findAllByPageName(pageName);
        User user = userRepository.findByEmail(userEmail);
        page.getLikedPageUsers().remove(user);
        pageRepository.save(page);
    }

    public List<Page> getCurrentUserPage(){
        return pageRepository.findAllByOwner(currentUser);
    }

    public List<Page> getAllPages(){
        return pageRepository.findAll();
    }

    private String getRandomNumber(){
        return String.valueOf(100000 + random.nextInt(900000));
    }
}
