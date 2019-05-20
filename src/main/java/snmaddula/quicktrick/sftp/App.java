package snmaddula.quicktrick.sftp;

import static org.apache.commons.io.IOUtils.toByteArray;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.Setter;

/**
 * 
 * @author snmaddula
 *
 */
@Setter
@RestController
@SpringBootApplication
@ConfigurationProperties("sftp")
public class App {

	private Resource prvkey;
	private String passphrase;
	private String username;
	private String host;
	private int port;
	
	@PostMapping("/push")
	public void push(MultipartFile file) throws Exception {
		String path = file.getOriginalFilename();
		sftp().put(file.getInputStream(), path);
	}

	@GetMapping("/pull")
	public void pull(String file, HttpServletResponse res) throws Exception {
		String fileName = file.indexOf("/") >= 0 ? file.substring(file.lastIndexOf("/") + 1) : file;
		res.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		res.getOutputStream().write(toByteArray(sftp().get(file)));
	}
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	private ChannelSftp sftp() throws Exception {
		JSch jsch = new JSch();
		jsch.addIdentity(prvkey.getFile().getAbsolutePath(), passphrase);
		Session s = jsch.getSession(username, host, port);
		s.setConfig("StrictHostKeyChecking", "no");
		s.connect();
		Channel channel = s.openChannel("sftp");
        channel.connect();
        ChannelSftp channelSftp = (ChannelSftp) channel;
        return channelSftp;
	}
}
