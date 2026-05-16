package SuperiorPro.SuperiorPOS.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import SuperiorPro.SuperiorPOS.DTO.UserDTO;
import SuperiorPro.SuperiorPOS.entity.User;

@Mapper
public interface UserMapper {

	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
	@Mapping(source = "username", target = "userName")
	@Mapping(source = "phoneNumber", target = "phoneNumber")
	User toUser(UserDTO userDTO);

	@Mapping(source = "userName", target = "username")
	@Mapping(source = "phoneNumber", target = "phoneNumber")
	UserDTO toUserDTO(User user);
}
