package com.example.lineechorobot.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lineechorobot.handler.MessageHandler;

@RequestMapping("/robot")
@RestController
public class RobotController {

	@Value("722ec8c5cc39f700a5f3690df0a5741a")
	private String LINE_SECRET;

	@Autowired
	private MessageHandler messageHandler;
	@GetMapping("/test")
	public ResponseEntity test() {
		return new ResponseEntity("Hello J A V A!!", HttpStatus.OK);
	}

	@PostMapping("/messaging")
	public ResponseEntity messagingAPI(@RequestHeader("x-line-signature") String x_line_signature,
									   @RequestBody String requestBody) throws UnsupportedEncodingException, IOException {
		if (checkFromLine(requestBody, x_line_signature)) {
			System.out.println("驗證通過");
			JSONObject object = new JSONObject(requestBody);
			for (int i = 0; i < object.getJSONArray("events").length(); i++) {
				if (object.getJSONArray("events").getJSONObject(i).getString("type").equals("message")) {
					messageHandler.doAction(object.getJSONArray("events").getJSONObject(i));
				}
			}
			return new ResponseEntity<String>("OK", HttpStatus.OK);
		}
		System.out.println("驗證不通過");
		return new ResponseEntity<String>("Not line platform", HttpStatus.BAD_GATEWAY);
	}

	public boolean checkFromLine(String requestBody, String x_line_signature) {
		SecretKeySpec key = new SecretKeySpec(LINE_SECRET.getBytes(), "HmacSHA256");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			byte[] source = requestBody.getBytes("UTF-8");
			String signature = Base64.encodeBase64String(mac.doFinal(source));
			if (signature.equals(x_line_signature)) {
				return true;
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
}
