package com.acs.bookingsystem;

import com.acs.bookingsystem.user.controller.UserController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingSystemApplicationTests {

	@Autowired
	UserController userController;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(userController);
	}

}
