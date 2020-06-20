package edu.csula.rubrics.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.csula.rubrics.models.Criterion;
import edu.csula.rubrics.models.Rating;
import edu.csula.rubrics.models.Rubric;
import edu.csula.rubrics.models.Tag;
import edu.csula.rubrics.models.User;
import edu.csula.rubrics.models.dao.CriterionDao;
import edu.csula.rubrics.models.dao.RubricDao;
import edu.csula.rubrics.models.dao.UserDao;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserDao userDao;

	// get ALL rubrics
	@GetMapping
	public List<User> getUsers(ModelMap models) {
		return userDao.getAllUsers();
	}

	// register
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public Long addUser(@RequestBody User user) {
		System.out.println("name: "+user.getUsername());
		System.out.println("pwd: "+user.getPassword());
		user = userDao.saveUser(user);
		return user.getId();
	}

	//login
	@PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user, HttpServletResponse res) {
    	User u = userDao.getUserByUsername(user.getUsername());
		if (u == null || !u.getPassword().equals(user.getPassword()))
			return ResponseEntity.ok().body("invalid user");
		
		String token = getJWTToken(u);
        Cookie cookie = new Cookie("rubric-alice-token", token);
//        cookie.setPath("/");
//        cookie.setHttpOnly(false);
//        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.addCookie(cookie);
        return ResponseEntity.ok().body(token);
    }

	private String getJWTToken(User user) {
		String secretKey = "alice-rubric-secret!";
		
		String token = Jwts.builder().setId(user.getId().toString()).setSubject("alice-rubric")
				.claim("username", user.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 600000))
				.signWith(SignatureAlgorithm.HS512, secretKey.getBytes()).compact();

		return token;
	}

	//return userId back and this should store in front-end state.
	@GetMapping("/loginStatus")
	public Long loginStatus(@CookieValue(value="rubric-alice-token", defaultValue="null") String jwt) {
		//jwt is the token generated by RubricService
		System.out.println("get cookie: "+jwt);
		//call canvas API.
		if(jwt==null||jwt.length()==0||jwt.equals("null"))
			return (long)-1;
		
		try {
			Jws<Claims> claims = Jwts.parser()
			  .setSigningKey("alice-rubric-secret!".getBytes("UTF-8"))
			  .parseClaimsJws(jwt);
			String username = claims.getBody().get("username").toString();
			return Long.parseLong(claims.getBody().getId());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return (long)-1;
		
	}
//	// get this rubric
//	@GetMapping("/{id}")
//	public Rubric getRubric(@PathVariable Long id) {
//		Rubric rubric = rubricDao.getRubric(id);
//		if (rubric == null)
//			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubric not found");
//		return rubric;
//	}

}
