package nhom17.OneShop.service;

import nhom17.OneShop.entity.Address;
import nhom17.OneShop.entity.User;

public interface AddressService {
    Address getValidatedShippingAddress(Integer addressId, User user);

    String formatFullAddress(Address address);
}

