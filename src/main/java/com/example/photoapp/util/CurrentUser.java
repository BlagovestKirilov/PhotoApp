package com.example.photoapp.util;

import com.example.photoapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component("currentUser")
@SessionScope
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    User user;
}
