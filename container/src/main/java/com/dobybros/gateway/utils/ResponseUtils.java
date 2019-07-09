package com.dobybros.gateway.utils;
//package com.dobybros.gateway.utils;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import javax.annotation.Resource;
//
//import org.bson.Document;
//
//import com.dobybros.chat.data.DeviceToken;
//import com.dobybros.chat.data.UserBase.Account;
//import chat.errors.CoreException;
//import chat.logs.LoggerEx;
//import chat.utils.AutoReloadProperties;
//import chat.utils.CleanDocument;
//
//import com.dobybros.gateway.data.StickerCategory;
//import com.talentchat.services.IOfflineMessageService;
//import com.talentcore.data.Comment;
//import com.talentcore.data.Moment;
//import com.talentcore.data.StickerSuit;
//import com.talentcore.data.Topic;
//import com.talentcore.data.User;
//import com.talentcore.rest.data.MediaResource;
//import com.talentcore.rest.data.StickerSuitRequest;
//import com.talentcore.rest.data.UserRequest;
//import com.talentcore.services.IUserService;
//import com.dobybros.chat.utils.ResponseCoreUtils;
//
//public class ResponseUtils extends ResponseCoreUtils{
//	private static final String TAG = ResponseUtils.class.getSimpleName();
//	@Resource
//	private IUserService userService;
//	@Resource
//	private IOfflineMessageService topicService;
//	@Resource
//	private AutoReloadProperties version;
//	
//	private static ResponseUtils instance;
//	public ResponseUtils() { 
//		instance = this;
//	}
//	
//	static final String FIELD_CONTACTGROUP_NAME = "name";
//	static final String FIELD_CONTACTGROUP_USERS = "users";
//	static final String FIELD_CONTACTGROUP_CREATOR = "creator";
//	static final String FIELD_CONTACTGROUP_UPDATETIME = "updateTime";
//	static final String FIELD_CONTACTGROUP_CREATETIME = "createTime";
//	private static final String FIELD_CONTACTGROUP_TYPE = "type";
//	private static final String FIELD_CONTACTGROUP_ID = "id";
//	
//	static final String FIELD_TOPIC_CONTENT = "content";
//	static final String FIELD_TOPIC_COMEFROM = "from";
//	static final String FIELD_TOPIC_CONTENTTYPE = "contentType";
//	static final String FIELD_TOPIC_TYPE = "type";
//	static final String FIELD_TOPIC_CREATETIME = "createTime";
//	static final String FIELD_TOPIC_UPDATETIME = "updateTime";
//	static final String FIELD_TOPIC_PARENTTOPICID = "cgid";
//	static final String FIELD_TOPIC_SEQUENCE = "seq";
//	static final String FIELD_TOPIC_USER = "user";
//	static final String FIELD_TOPIC_ID = "id";
//	static final String FIELD_TOPIC_LONGITUDE = "lo";
//	static final String FIELD_TOPIC_LATITUDE = "la";
//	static final String FIELD_TOPIC_CHATTITLE = "title";
//	static final String FIELD_TOPIC_CHATICON = "icon";
//	static final String FIELD_TOPIC_USERNAME = "userName";
//	static final String FIELD_TOPIC_DELETED = "deleted";
//	static final String FIELD_TOPIC_PARTICIPANTIDS = "pids";
//	
//	public static Document getTopic(Topic topic, Integer version, String terminal) {
//		final Document topicObj = new CleanDocument();
//		final String userId = topic.getUserId();
//		User user = null;
//		if(userId != null) {
//			try {
//				user = instance.userService.getUser(userId);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
//		topicObj.put(FIELD_TOPIC_CONTENT, topic.getContent());
//		topicObj.put(FIELD_TOPIC_COMEFROM, topic.getComeFrom());
//		topicObj.put(FIELD_TOPIC_CONTENTTYPE, topic.getContentType());
//		topicObj.put(FIELD_TOPIC_TYPE, topic.getType());
//		topicObj.put(FIELD_TOPIC_CREATETIME, topic.getCreateTime());
//		topicObj.put(FIELD_TOPIC_PARENTTOPICID, topic.getParentTopicId());
//		topicObj.put(FIELD_TOPIC_UPDATETIME, topic.getUpdateTime());
//		topicObj.put(FIELD_TOPIC_ID, topic.getId());
////		topicObj.put(FIELD_TOPIC_PARTICIPANTIDS, topic.getParticipantIds());
//		List<Double> lola = topic.getLongitudeLatitude();
//		if(lola != null && lola.size() == 2) { 
//			topicObj.put(FIELD_TOPIC_LONGITUDE, lola.get(0));
//			topicObj.put(FIELD_TOPIC_LATITUDE, lola.get(1));
//		}
//		if(user != null) {
//			final Document userObj = getUser(user);
//			ResponseUtils.handleVersionDependency("minUserIdVersion", version, terminal, new DependencyHandler() {
//				
//				@Override
//				public void unSupported() {
//					topicObj.put(FIELD_TOPIC_USER, userObj);
//				}
//				
//				@Override
//				public void supported() {
//					topicObj.put(FIELD_TOPIC_USER, new Document("id", userId));
//				}
//			});
//		}
//		return topicObj;
//	}
//	
//	public static Document getTopic(Topic topic) {
//		return getTopic(topic, null, null);
//	}
//	
//	public static Document getMoment(Moment moment, String checkLikedId, Integer version, String terminal) {
//		final AtomicBoolean supported = new AtomicBoolean(false);
//		ResponseUtils.handleVersionDependency("minUserIdVersion", version, terminal, new DependencyHandler() {
//			
//			@Override
//			public void unSupported() {
//				supported.set(false);
//			}
//			
//			@Override
//			public void supported() {
//				supported.set(true);
//			}
//		});
//		boolean support = supported.get();
//		
//		Document momentObj = new CleanDocument();
//		String userId = moment.getUserId();
//		User user = null;
//		if(userId != null) {
//			try {
//				user = instance.userService.getUser(userId);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		momentObj.put("id", moment.getId());
//		if(user != null) {
//			if(support)
//				momentObj.put(Moment.FIELD_USER, getUser(user, version, terminal));
//			else
//				momentObj.put(Moment.FIELD_USER, getUser(user));
//		}
//		momentObj.put(Moment.FIELD_CITY, moment.getCity());		//city, state, country
//		if(moment.getIcons() != null) {
//			List<Document> iconList = new ArrayList<Document>();
//			for(MediaResource mr : moment.getIcons()) {
//				iconList.add(mr.toDocument());
//			}
//			momentObj.put(UserRequest.FIELD_USER_ICONS, iconList);
//		}
//		momentObj.put(Moment.FIELD_TEXT, moment.getText());
//		momentObj.put("fontColor", moment.getFontColor());
//		momentObj.put("backgroundColor", moment.getBackgroundColor());
//		momentObj.put("postTime", moment.getPostTime());
//		boolean liked = false;
//		Collection<String> likeIds = moment.getLikeIds();
//		Integer likeCount = 0;
//		if(moment.getLikeCounts() != null){
//			likeCount = moment.getLikeCounts();
//		}
//		if(likeIds != null) {
////			if(likeIds.contains(checkLikedId))
////				liked = true;
//			//组织likeUserList
//			User likeUser = null;
//			List<Document> likeUserList = new ArrayList<Document>();
//			for(String likeId : likeIds) {
//				try {
//					likeUser = instance.userService.getUser(likeId);
//					if(likeUser != null) {
//						if(support)
//							likeUserList.add(getUser(likeUser, version, terminal));
//						else
//							likeUserList.add(getUser(likeUser));
//					} else {
//						LoggerEx.warn(TAG, "User : " + likeUser + " is null.");
//					}
//				} catch (CoreException e) {
//					e.printStackTrace();
//				}
//			}
//			if(!likeUserList.isEmpty()) {
//				momentObj.put("likeUsers", likeUserList);
//			}
//		}
//		momentObj.put("likeCount", likeCount);
////		momentObj.put("liked", liked);
//		momentObj.put("commentCount", moment.getCommentCounts());
////		momentObj.put("locateBy", moment.getLocateBy());
////		Double[] lola = moment.getLongitudeLatitude();
////		if(lola != null && lola.length == 2) { 
////			momentObj.put(FIELD_TOPIC_LONGITUDE, lola[0]);
////			momentObj.put(FIELD_TOPIC_LATITUDE, lola[1]);
////		}
//		return momentObj;
//	}
//	
//	/**
//	 * 组织moment输出对象，取user放入
//	 * @param moment
//	 * @param checkLikedId	根据此userid判断是否like了这条moment
//	 * @return
//	 */
//	public static Document getMoment(Moment moment, String checkLikedId) {
//		Document momentObj = new CleanDocument();
//		String userId = moment.getUserId();
//		User user = null;
//		if(userId != null) {
//			try {
//				user = instance.userService.getUser(userId);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		momentObj.put("id", moment.getId());
//		if(user != null)
//			momentObj.put(Moment.FIELD_USER, getUser(user));
//		momentObj.put(Moment.FIELD_CITY, moment.getCity());		//city, state, country
//		if(moment.getIcons() != null) {
//			List<Document> iconList = new ArrayList<Document>();
//			for(MediaResource mr : moment.getIcons()) {
//				iconList.add(mr.toDocument());
//			}
//			momentObj.put(UserRequest.FIELD_USER_ICONS, iconList);
//		}
//		momentObj.put(Moment.FIELD_TEXT, moment.getText());
//		momentObj.put("fontColor", moment.getFontColor());
//		momentObj.put("backgroundColor", moment.getBackgroundColor());
//		momentObj.put("postTime", moment.getPostTime());
//		boolean liked = false;
//		Collection<String> likeIds = moment.getLikeIds();
//		Integer likeCount = 0;
//		if(moment.getLikeCounts() != null){
//			likeCount = moment.getLikeCounts();
//		}
//		if(likeIds != null) {
////			if(likeIds.contains(checkLikedId))
////				liked = true;
//			//组织likeUserList
//			User likeUser = null;
//			List<Document> likeUserList = new ArrayList<Document>();
//			for(String likeId : likeIds) {
//				try {
//					likeUser = instance.userService.getUser(likeId);
//					likeUserList.add(getUser(likeUser));
//				} catch (CoreException e) {
//					e.printStackTrace();
//				}
//			}
//			if(!likeUserList.isEmpty()) {
//				momentObj.put("likeUsers", likeUserList);
//			}
//		}
//		momentObj.put("likeCount", likeCount);
////		momentObj.put("liked", liked);
//		momentObj.put("commentCount", moment.getCommentCounts());
////		momentObj.put("locateBy", moment.getLocateBy());
////		Double[] lola = moment.getLongitudeLatitude();
////		if(lola != null && lola.length == 2) { 
////			momentObj.put(FIELD_TOPIC_LONGITUDE, lola[0]);
////			momentObj.put(FIELD_TOPIC_LATITUDE, lola[1]);
////		}
//		return momentObj;
//	}
//	
//	/**
//	 * moment的配图(包含大、小图)
//	 */
//	public static final String FIELD_ICONS = "icons";
//	/**
//	 * 文字内容
//	 */
//	public static final String FIELD_TEXT = "text";
//	/**
//	 * 字体颜色
//	 */
//	public static final String FIELD_FONT_COLOR = "fontColor";
//	/**
//	 * 背景颜色(不传图片时)
//	 */
//	public static final String FIELD_BACKGROUND_COLOR = "backgroundColor";
//	
//	/**
//	 * 评论内容
//	 */
//	public static final String FIELD_COMMENT = "comment";
//	/**
//	 * 关联的momentId
//	 */
//	public static final String FIELD_MOMENT_ID = "momentId";
//	/**
//	 * 评论者的城市信息
//	 */
//	public static final String FIELD_CITY = "city";
//	/**
//	 * 回复目标的楼层
//	 */
//	public static final String FIELD_TARGET_FLOOR = "targetFloor";
//	/**
//	 * 回复目标的id
//	 */
//	public static final String FIELD_TARGET_ID = "targetId";
//	/**
//	 * 所回复评论的摘要
//	 */
//	public static final String FIELD_SUMMARY = "summary";
//	/**
//	 * 位置来源
//	 */
//	public static final String FIELD_LOCATE_BY = "locateBy";
//	/**
//	 * 坐标位置
//	 */
//	public static final String FIELD_LONGITUDELATITUDE = "location";
//	/**
//	 * 组织comment输出对象，取user放入
//	 * @param comment
//	 * @param version
//	 * @param terminal
//	 * @return
//	 */
//	public static Document getComment(Comment comment, Integer version, String terminal) {
//		final Document commentObj = new CleanDocument();
//		//获取user对象
//		final String userId = comment.getUserId();
//		User user = null;
//		if(userId != null) {
//			try {
//				user = instance.userService.getUser(userId);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
//		//获取targetuser对象
//		final String targetUserId = comment.getTargetId();
//		User targetUser = null;
//		if(targetUserId != null) {
//			try {
//				targetUser = instance.userService.getUser(targetUserId);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		commentObj.put("id", comment.getId());
//		commentObj.put(FIELD_MOMENT_ID, comment.getMomentId());
////		commentObj.put("floor", comment.getFloor());
//		if(user != null) {
//			final Document userObj = getUser(user);
//			ResponseUtils.handleVersionDependency("minUserIdVersion", version, terminal, new DependencyHandler() {
//				
//				@Override
//				public void unSupported() {
//					commentObj.put("user", userObj);
//				}
//				
//				@Override
//				public void supported() {
//					commentObj.put("user", new Document("id", userId));
//				}
//			});
//		}
//		
//		
//		
//		
//		commentObj.put(Comment.FIELD_CITY, comment.getCity());		//city, state, country
////		commentObj.put(CommentRequest.FIELD_TARGET_FLOOR, comment.getTargetFloor());
////		commentObj.put(CommentRequest.FIELD_TARGET_ID, comment.getTargetId());
//		if(targetUser != null) {
//			final Document targetUserObj = getUser(targetUser);
//			ResponseUtils.handleVersionDependency("minUserIdVersion", version, terminal, new DependencyHandler() {
//				
//				@Override
//				public void unSupported() {
//					commentObj.put("targetUser", targetUserObj);
//				}
//				
//				@Override
//				public void supported() {
//					commentObj.put("targetUser", new Document("id", targetUserId));
//				}
//			});
//		}
//		commentObj.put(FIELD_COMMENT, comment.getComment());
////		commentObj.put(CommentRequest.FIELD_SUMMARY, comment.getSummary());
//		commentObj.put(Comment.FIELD_TIME, comment.getTime());
//		return commentObj;
//	}
//	/**
//	 * 组织comment输出对象，取user放入
//	 * @param comment
//	 * @return
//	 */
//	public static Document getComment(Comment comment) {
//		return getComment(comment, null, null);
//	}
//
//	public static IUserService getUserService() {
//		return instance.userService;
//	}
//
///*    public static Document getStickerCategory(StickerCategory stickerCategory) {
//        return new CleanDocument().append("id", stickerCategory.getId()).append("name", stickerCategory.getName());
//    }
//
//    public static List<Document> getStickerCategories(List<StickerCategory> categories) {
//        List<Document> list = new ArrayList<Document>();
//        if (categories != null) {
//            for (StickerCategory cg : categories) {
//                list.add(getStickerCategory(cg));
//            }
//        }
//        return list;
//    }*/
//
//	private static final String FIELD_USER_UPDATETIME = "updateTime";
//	private static final String FIELD_USER_HEADICON = "icon";
//	private static final String FIELD_USER_ID = "id";
//	private static final String FIELD_USER_REGISTERTIME = "registerTime";
//	private static final String FIELD_USER_TYPE = "type";
//	
//	public static Document getUserById(String userId) {
//		User user = null;
//		try {
//			user = instance.userService.getUser(userId);
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//		return getUser(user, false);
//	}
//	public static Document getUser(User user) {
//		return getUser(user, false);
//	}
//	
//	public static Document getUser(User user, Integer version, String terminal) {
//		final AtomicBoolean supported = new AtomicBoolean(false);
//		ResponseUtils.handleVersionDependency("minUserIdVersion", version, terminal, new DependencyHandler() {
//			
//			@Override
//			public void unSupported() {
//				supported.set(false);
//			}
//			
//			@Override
//			public void supported() {
//				supported.set(true);
//			}
//		});
//		
//		if(supported.get())
//			return new Document(FIELD_USER_ID, user.getId());
//		else
//			return getUser(user, false);
//	}
//	
//	public static Document getUser(User user, Boolean needAccount) {
//		Document dbObj = new CleanDocument();
//		dbObj.put(UserRequest.FIELD_USER_DISPLAYNAME, user.getDisplayName());
//		dbObj.put(UserRequest.FIELD_USER_BIRTHDAY, user.getBirthday());
//		dbObj.put(UserRequest.FIELD_USER_COMPANY, user.getCompany());
//		dbObj.put(UserRequest.FIELD_USER_COUNTRY, user.getCountry());
//		dbObj.put(UserRequest.FIELD_USER_DESCRIPTION, user.getDescription());
//		dbObj.put(UserRequest.FIELD_USER_EMAILS, user.getEmails());
//		dbObj.put(UserRequest.FIELD_USER_EXPERTISE, user.getExpertise());
//		dbObj.put(UserRequest.FIELD_USER_GENDER, user.getGender());
//		dbObj.put(UserRequest.FIELD_USER_HAUNT, user.getHaunt());
//		if(user.getIcon() != null)
//			dbObj.put(FIELD_USER_HEADICON, user.getIcon().toDocument());
//		dbObj.put(UserRequest.FIELD_USER_HOMETOWN, user.getHometown());
//		if(user.getIcons() != null) {
//			List<Document> mrList = new ArrayList<Document>();
//			for(MediaResource mr : user.getIcons()) {
//				mrList.add(mr.toDocument());
//			}
//			dbObj.put(UserRequest.FIELD_USER_ICONS, mrList);
//		}
//		dbObj.put(FIELD_USER_ID, user.getId());
//		dbObj.put(UserRequest.FIELD_USER_INITIALS, user.getInitials());
//		dbObj.put(UserRequest.FIELD_USER_INTERESTS, user.getInterests());
//		dbObj.put(UserRequest.FIELD_USER_NAME, user.getName());
//		dbObj.put(UserRequest.FIELD_USER_NICKNAME, user.getNickName());
//		dbObj.put(UserRequest.FIELD_USER_PHONES, user.getPhones());
//		dbObj.put(UserRequest.FIELD_USER_PROFESSION, user.getProfession());
//		dbObj.put(FIELD_USER_REGISTERTIME, user.getRegisterTime());
//		dbObj.put(UserRequest.FIELD_USER_RELATION, user.getRelation());
//		dbObj.put(UserRequest.FIELD_USER_SCHOOL, user.getSchool());
//		dbObj.put(UserRequest.FIELD_USER_SIGNATURE, user.getSignature());
//		if(needAccount) {
//			List<Document> accsList = new ArrayList<Document>();
//			Account[] accs = user.getAccounts();
//			for(int i = 0;i < accs.length;i ++){
//				accsList.add(accs[i].toDocument());
//			}
//			dbObj.put(UserRequest.FIELD_USER_ACCOUNTS, accsList);
//		}
//		
//		dbObj.put(FIELD_USER_TYPE, user.getType());
//		dbObj.put(FIELD_USER_UPDATETIME, user.getUpdateTime());
//		return dbObj;
//	}
//	
///*    public static Document getStickerSuit(StickerSuit t, Boolean mine, String terminal) {
//    	Document user = null;
//        try {
//            user = getUser(instance.userService.getUser(t.getUploaderId()));
//        } catch (CoreException e) {
//            e.printStackTrace();
//        }
//        
//        CleanDocument dbo =
//                new CleanDocument().append("id", t.getId())
//                        .append("name", t.getName())
//                        .append("desc", t.getDesc())
//                        .append("expiredDate", t.getExpiredDate())
//                        .append("createTime", t.getCreateTime())
//                        .append("updateTime", t.getUpdateTime())
//                        .append("zipSize", t.getZipSize())
//                        .append("zipResourceId", t.getZipId())
//                        .append("thumbnail", t.getThumbnailId());
//        if (!DeviceToken.TERMINAL_ANDROID.equals(terminal) &&
//                !DeviceToken.TERMINAL_IOS.equals(terminal) &&
//                !DeviceToken.TERMINAL_WINDOWSPHONE.equals(terminal)) {
//            dbo.append("stickers", t.getStickers())
//            .append("uploader", user)
//            .append("backgrounds", t.getOverviewResIds());
//        }
//        if (!StickerCategory.DEFAULT_NIL_CATEGORY.getId().equals(t.getCategoryId())) {
//            dbo.append("categoryId", t.getCategoryId());
//        }
//        if (mine != null) {
//            dbo.append("mine", mine);
//        }
//        return dbo;
//    }*/
//
//    public static void handleVersionDependency(String featureName, Integer currentVersion, String terminal, DependencyHandler dh) {
//    	Integer minVersion = null;
//    	if(terminal == null)
//    		minVersion = Integer.MAX_VALUE;
//    	else 
//    		minVersion = Integer.parseInt(instance.version.getProperty(terminal + "." + featureName));
//    	if(dh == null)
//    		return;
//    	if(currentVersion == null)
//    		currentVersion = 0;
//		if(currentVersion > minVersion)
//			dh.supported();
//		else
//			dh.unSupported();
//    }
//    
//	public static IOfflineMessageService getTopicService() {
//		return instance.topicService;
//	}
//
//}
