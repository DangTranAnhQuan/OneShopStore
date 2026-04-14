package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.repository.AddressRepository;
import nhom17.OneShop.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	private AddressRepository addressRepository;

	@Override
	public Address getValidatedShippingAddress(Integer addressId, User user) {
		if (addressId == null) {
			throw new IllegalArgumentException("Địa chỉ giao hàng không hợp lệ.");
		}
		if (user == null || user.getUserId() == null) {
			throw new IllegalStateException("Người dùng chưa đăng nhập.");
		}
		return addressRepository.findById(addressId)
				.filter(address -> address.getUser() != null
						&& Objects.equals(address.getUser().getUserId(), user.getUserId()))
				.orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không hợp lệ."));
	}

	@Override
	public String formatFullAddress(Address address) {
		if (address == null) {
			return "";
		}
		return String.format("%s, %s, %s, %s",
				nullToEmpty(address.getStreetAddress()),
				nullToEmpty(address.getWard()),
				nullToEmpty(address.getDistrict()),
				nullToEmpty(address.getProvince()));
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
