package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> getUsers();

    Optional<User> getUserById(int userId);

    int addUser(User user);

    void updateUser(User user);

    void deleteUserById(int userId);

    void addFriendship(int userId, int friendId);

    void removeFriendship(int userId, int friendId);

    public List<User> getAllUsers();

    List<User> getUsersFriends(int id);

    List<User> getMutualFriends(int id, int otherId);

    boolean checkUserExist(int userId);

    List<Integer> getFriendsIdByUserId(int userId);

    void checkUser(final int userId);


}
