package com.dobybros.chat.utils;

import chat.logs.LoggerEx;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

public class Mail {
	private static final String TAG = Mail.class.getSimpleName();
	
	String to = ""; // 收件人
	String from = ""; // 发件人
	String sender = ""; // 发件人名称
	String host = ""; // smtp主机
	String username = ""; // 用户名
	String password = ""; // 密码
	String subject = ""; // 邮件主题
	String content = ""; // 邮件正文

	public Mail() {
	}

	/**
	 * 
	 * @param to	收件人
	 * @param from	发件人
	 * @param sender	发件人名称
	 * @param host	smtp主机
	 * @param username	用户名
	 * @param password	密码
	 * @param subject	邮件主题
	 * @param content	邮件正文
	 */
	public Mail(String to, String from, String sender, String host, String username,
                String password, String subject, String content) {
		this.to = to;
		this.from = from;
		this.sender = sender;
		this.host = host;
		this.username = username;
		this.password = password;
		this.subject = subject;
		this.content = content;
	}

	/**
	 * 把主题转换为中文
	 * 
	 * @param strText
	 * @return
	 */
	public String transferChinese(String strText) {

		try {
			strText = MimeUtility.encodeText(new String(strText.getBytes(), "UTF-8"), "UTF-8", "B");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strText;
	}

	/**
	 * 发送邮件
	 * 
	 * @return 成功返回true，失败返回false
	 */
	public boolean sendMail() {
		// 构造mail session
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(props,
				new Authenticator() {
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
		try {
			// 构造MimeMessage并设定基本的值，创建消息对象
			MimeMessage msg = new MimeMessage(session);
			// 设置消息内容
			msg.setFrom(new InternetAddress(from, sender));
			System.out.println(from);
			// 把邮件地址映射到Internet地址上
			InternetAddress[] address = { new InternetAddress(to) };
			/**
			 * setRecipient（Message.RecipientType type, Address
			 * address），用于设置邮件的接收者。<br>
			 * 有两个参数，第一个参数是接收者的类型，第二个参数是接收者。<br>
			 * 接收者类型可以是Message.RecipientType .TO，Message
			 * .RecipientType.CC和Message.RecipientType.BCC，TO表示主要接收人，CC表示抄送人
			 * ，BCC表示秘密抄送人。接收者与发送者一样，通常使用InternetAddress的对象。
			 */
			msg.setRecipients(Message.RecipientType.TO, address);
			// 设置邮件的标题
			subject = transferChinese(subject);
			msg.setSubject(subject);
			// 构造Multipart
			Multipart mp = new MimeMultipart();

			// 向Multipart添加正文
			MimeBodyPart mbpContent = new MimeBodyPart();
			// 设置邮件内容(纯文本格式)
			// mbpContent.setText(content);
			// 设置邮件内容(HTML格式)
			mbpContent.setContent(content, "text/html;charset=utf-8");
			// 向MimeMessage添加（Multipart代表正文）
			mp.addBodyPart(mbpContent);
			// 向Multipart添加MimeMessage
			msg.setContent(mp);
			// 设置邮件发送的时间。
			msg.setSentDate(new Date());
			// 发送邮件
			Transport.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
			LoggerEx.error(TAG, "send email failed." +  e.getMessage());
			return false;
		}
		return true;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * 生成正文html
	 * @param fname 昵称
	 * @param url	修改密码链接
	 * @param helpAccount	help邮箱
	 * @return
	 */
	public static String getContentInHtml(String fname, String url, String helpAccount) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='width: 800px;border-color: #3398ff;border-width: 1px;border-style: solid;color: #ffffff;'>");
		sb.append("<div style='width: 800px;height: 80px;background: #3398ff;line-height: 80px;text-align: center;font-size: 24px;font-weight: bold;'>Miss</div>");
		sb.append("<div style='width: 800px;text-indent: 1em;'>");
		sb.append("<h2 style='color: #333333;'>Hello "+ fname + "</h2>");
		sb.append("<p style='color: #333333;'>You're receiving this email because you requested to reset your Miss password</p>");
		sb.append("<p style='color: #333333;'>Please change your password via the following link within 24 hours :</p>");
		sb.append("<p style='color: #333333;'><a href='" + url + "'>" + url + "</a></p>");
		sb.append("<p style='font-weight:bold;color: #333333;'>Any question, please let us know by contacting <a href='mailto:" + helpAccount + "'>" + helpAccount + "</a>. Thanks!</p>");
		sb.append("<p style='color: #333333;'>See you soon,</p>");
		sb.append("<p style='color: #333333;'>The Miss team</p>");
		sb.append("</div>");
		sb.append("<div style='width: 800px;height: 40px;background: #3398ff;line-height: 40px;text-align: center;color: #ffffff;'>Terms | Privacy</div>");
		sb.append("<div style='width: 800px;height: 40px;background: #3398ff;line-height: 40px;text-align: center;color: #ffffff;'>KEAI Technology Co., Ltd. Copyright©2015-2016</div>");
		sb.append("</div>");
		return sb.toString();
	}
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style='width: 800px;border-color: #3398ff;border-width: 1px;border-style: solid;color: #ffffff;'>");
		sb.append("<div style='width: 800px;height: 80px;background: #3398ff;line-height: 80px;text-align: center;font-size: 24px;font-weight: bold;'>Miss</div>");
		sb.append("<div style='width: 800px;text-indent: 1em;'>");
		sb.append("<h2 style='color: #333333;'>Hello *****</h2>");
		sb.append("<p style='color: #333333;'>You're receiving this email because you requested to reset your Miss password</p>");
		sb.append("<p style='color: #333333;'>Please change your password via the following link within 24 hours :</p>");
		sb.append("<p style='color: #333333;'><a href='http://localhost:8011/rest/sys/status'>http://localhost:8011/rest/sys/status</a></p>");
		sb.append("<p style='font-weight:bold;color: #333333;'>Any question, please let us know by contacting <a href='mailto:help@miss.com'>help@miss.com</a>. Thanks!</p>");
		sb.append("<p style='color: #333333;'>See you soon,</p>");
		sb.append("<p style='color: #333333;'>The Miss team</p>");
		sb.append("</div>");
		sb.append("<div style='width: 800px;height: 40px;background: #3398ff;line-height: 40px;text-align: center;color: #ffffff;'>Terms | Privacy</div>");
		sb.append("<div style='width: 800px;height: 40px;background: #3398ff;line-height: 40px;text-align: center;color: #ffffff;'>Miss Inc. Copyright©2015-2016</div>");
		sb.append("</div>");
		Mail mail = new Mail("rbh920rbh@126.com", "immissapp@talentwalker.com", "Miss Team", "smtp.exmail.qq.com", "immissapp@talentwalker.com", "Chat@Talent123", "Reset Your Miss Password", sb.toString());
		mail.sendMail();
	}
}
