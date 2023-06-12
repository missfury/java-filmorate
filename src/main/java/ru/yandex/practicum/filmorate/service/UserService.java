package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotExistException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validate.UserValidate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Integer userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotExistException("Пользователя с id: " + userId + " не сушествует"));
    }

    public User addUser(User user) {
        UserValidate.validate(user);
        int newId = userStorage.addUser(user);
        return userStorage.getUserById(newId)
                .orElseThrow(() -> new NotExistException("Пользователя с id: " + newId + " не существует"));
    }

    public User updateUser(User user) {
        UserValidate.validate(user);
        throwIfUserNotExist(List.of(user.getId()));
        userStorage.updateUser(user);
        return userStorage.getUserById(user.getId())
                .orElseThrow(() -> new NotExistException("Пользователя с id: " + user.getId() + " не  существует"));
    }

    public void deleteUserById(int userId) {
        throwIfUserNotExist(List.of(userId));
        userStorage.deleteUserById(userId);
    }

    boolean containsFriendId(int userId, int friendId) {
        return userStorage.getFriendsIdByUserId(userId).contains(friendId);
    }

    public User addFriend(int userId, int friendId) {
        checkUsersDifferent(userId, friendId);
        throwIfUserNotExist(List.of(userId, friendId));
        if (containsFriendId(userId,friendId))
            throw new ValidationException("Пользователи с id " + userId + " и " + friendId + " уже друзья");
        userStorage.addFriendship(userId, friendId);
        log.info("Пользователи с id: {} и {} стали друзьями.", userId, friendId);
        return getUserById(userId);
    }

    public User deleteFriend(int userId, int friendId) {
        checkUsersDifferent(userId, friendId);
        throwIfUserNotExist(List.of(userId, friendId));
        if (!containsFriendId(userId,friendId))
            throw new ValidationException("Пользователи с id " + userId + " и " + friendId + " не друзья");
        userStorage.removeFriendship(userId, friendId);
        log.info("Пользователи с id: {} и {} больше не друзья.", userId, friendId);
        return getUserById(userId);
    }

    public List<User> getUserFriends(int userId) {
        throwIfUserNotExist(List.of(userId));
        return userStorage.getUsersFriends(userId);
    }

    public List<User> getCommonFriends(int firstUserId, int secondUserId) {
        throwIfUserNotExist(List.of(firstUserId, secondUserId));
        checkUsersDifferent(firstUserId, secondUserId);
        return userStorage.getMutualFriends(firstUserId, secondUserId);
    }

    public void throwIfUserNotExist(List<Integer> userIdList) {
        for (Integer userId : userIdList) {
            if (!userStorage.checkUserExist(userId))
                throw new NotExistException("Пользователя с id: " + userId + " не существует");
        }
    }

    private void checkUsersDifferent(int firstUserId, int secondUserId) {
        if (firstUserId == secondUserId)
            throw new ValidationException("Пользователь не может быть другом самому себе");
    }
}