package com.github.brandonbai.springmvcueditordemo.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author brandonbai
 *
 */
@Controller
public class UEditorController {

	private static final String DIR_NAME = "~/Desktop";
	
	private static final String PREFIX = "/editor/image";
	
	private static final String FILE_SEPARATOR = File.separator;
	
	private static final String PATH_SEPARATOR = "/";
	
	private static final String PATH_FORMAT = "yyyyMMddHHmmss";
	
	private static final String CONFIG_FILE_NAME = "config.json";
	
	private static final String ACTION_NAME_CONFIG = "config";
	
	private static final String ACTION_NAME_UPLOAD_IMAGE = "uploadimage";
	
	private static final Logger logger = LoggerFactory.getLogger(UEditorController.class);

	/**
	 * 配置、图片处理
	 */
	@RequestMapping("/ueConvert")
	public void ueditorConvert(HttpServletRequest request, HttpServletResponse response, String action,
			MultipartFile upfile) {

		try {
			request.setCharacterEncoding("utf-8");
			response.setHeader("Content-Type", "text/html");
			PrintWriter pw = response.getWriter();
			if (ACTION_NAME_CONFIG.equals(action)) {
				String content = readFile(this.getClass().getResource(PATH_SEPARATOR).getPath() + CONFIG_FILE_NAME);
				pw.write(content);
			} else if (ACTION_NAME_UPLOAD_IMAGE.equals(action)) {
				Map<String, Object> map = new HashMap<String, Object>(16);
				String time = new SimpleDateFormat(PATH_FORMAT).format(new Date());
				try {
					String originalFilename = upfile.getOriginalFilename();
					String type = originalFilename.substring(originalFilename.lastIndexOf("."));
					String dirName = DIR_NAME + PREFIX + FILE_SEPARATOR + time;
					File dir = new File(dirName);
					if(!dir.exists() || !dir.isDirectory()) {
						dir.mkdirs();
					}
					String fileName = dirName + FILE_SEPARATOR + originalFilename;
					upfile.transferTo(new File(fileName));
					map.put("state", "SUCCESS");
					map.put("original", originalFilename);
					map.put("size", upfile.getSize());
					map.put("title", fileName);
					map.put("type", type);
					map.put("url", "." + PREFIX + PATH_SEPARATOR + time + PATH_SEPARATOR + originalFilename);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("upload file error", e);
					map.put("state", "error");
				}
				response.setHeader("Content-Type", "application/json");
				pw.write(JSON.toJSONString(map));
				pw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 图片读取
	 */
	@RequestMapping(PREFIX + "/{time}/{path}.{type}")
	public void ueditorConvert(@PathVariable("time") String time, @PathVariable("path") String path,
			@PathVariable("type") String type, HttpServletRequest request, HttpServletResponse response) {
		try (FileInputStream fis = new FileInputStream(DIR_NAME + PREFIX + PATH_SEPARATOR + time + PATH_SEPARATOR + path + "." + type)) {
			int len = fis.available();
			byte[] data = new byte[len];
			fis.read(data);
			fis.close();
			ServletOutputStream out = response.getOutputStream();
			out.write(data);
			out.close();
		} catch (Exception e) {
			logger.error("read file error", e);
		}

	}

	private String readFile(String path) {

		StringBuilder builder = new StringBuilder();

		try(BufferedReader bfReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {

			String tmpContent = null;

			while ((tmpContent = bfReader.readLine()) != null) {
				builder.append(tmpContent);
			}

			bfReader.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return builder.toString().replaceAll("/\\*[\\s\\S]*?\\*/", "");

	}

}
