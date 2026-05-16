package SuperiorPro.SuperiorPOS.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import SuperiorPro.SuperiorPOS.DTO.RoleDTO;
import SuperiorPro.SuperiorPOS.entity.Role;

public interface RoleService {

    Role save(RoleDTO roleDTO);

    Role getById(Long id);

    Page<Role> getRoles(Pageable pageable);

    Role updateById(Long id, RoleDTO dto);

    void deleteById(Long id);
}
