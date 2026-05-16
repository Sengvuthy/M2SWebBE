package SuperiorPro.SuperiorPOS.service;

import java.util.Map;

import org.springframework.data.domain.Page;

import SuperiorPro.SuperiorPOS.DTO.UserDTO;
import SuperiorPro.SuperiorPOS.entity.User;

public interface UserService {

	User save(UserDTO userTO);
    User getById(Long id);
    User getByName(String name);
    Page<User> getUsers(Map<String, String> param);
    User updateById(Long id, UserDTO dto);
    User update(String name, UserDTO userDTO);
    void deleteById(Long id);
    void deleteByName(String name);
}
