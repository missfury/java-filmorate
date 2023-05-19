package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validate.UserValidate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Integer userId) {
        return userStorage.getUserById(userId);
    }

    public User addUser(User user) {
        UserValidate.validate(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        UserValidate.validate(user);
        return userStorage.updateUser(user);
    }

    public User deleteUserById(int userId) {
        checkUserExist(List.of(userId));
        return userStorage.deleteUserById(userId);
    }

    public User addFriend(int userId, int friendId) {
        if (userId == friendId)
            throw new ValidationException("Пользователь не может быть другом самому себе.");
        checkUserExist(List.of(userId,friendId));
        userStorage.getUserById(userId).getFriends().add(friendId);
        userStorage.getUserById(friendId).getFriends().add(userId);
        log.info("Пользователи с id: {} и {} стали друзьями.", userId, friendId);
        return userStorage.getUserById(userId);
    }

    public User deleteFriend(int userId, int friendId) {
        if (userId == friendId)
            throw new ValidationException("Пользователь не может быть другом самому себе.");
        checkUserExist(List.of(userId,friendId));
        if (!userStorage.getUserById(userId).getFriends().contains(friendId) && !userStorage.getUserById(friendId).getFriends().contains(userId))
            throw new ValidationException("Пользователи с id {} и {} больше не друзья.");
        userStorage.getUserById(userId).getFriends().remove(friendId);
        userStorage.getUserById(friendId).getFriends().remove(userId);
        log.info("Пользователи с id: {} и {} больше не друзья.", userId, friendId);
        return userStorage.getUserById(userId);
    }

    public List<User> getUserFriends(int userId) {
        checkUserExist(List.of(userId));
        return userStorage.getUserById(userId).getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int firstUserId, int secondUserId) {
        checkUserExist(List.of(firstUserId,secondUserId));
        if (firstUserId == secondUserId)
            throw new ValidationException("Пользователь не может быть другом самому себе.");
        return userStorage.getUserById(firstUserId).getFriends().stream()
                .filter(friendId -> userStorage.getUserById(secondUserId).getFriends().contains(friendId))
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    private void checkUserExist(List<Integer> userIdList){
        for (Integer userId : userIdList){
            if (!userStorage.getUsersMap().containsKey(userId))
                throw new NotExistException("Пользователя с id: " + userId + " не существует.");
        }
    }
}