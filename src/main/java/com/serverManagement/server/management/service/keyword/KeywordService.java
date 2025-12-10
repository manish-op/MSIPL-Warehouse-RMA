package com.serverManagement.server.management.service.keyword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serverManagement.server.management.dao.admin.user.AdminUserDAO;
import com.serverManagement.server.management.dao.keyword.KeywordDAO;
import com.serverManagement.server.management.dao.keyword.SubKeywordDAO;
import com.serverManagement.server.management.entity.adminUser.AdminUserEntity;
import com.serverManagement.server.management.entity.keyword.KeywordEntity;
import com.serverManagement.server.management.entity.keyword.SubKeywordEntity;
import com.serverManagement.server.management.globalException.NullValueException;
import com.serverManagement.server.management.request.keyword.GetSubkeywordRequest;
import com.serverManagement.server.management.request.keyword.KeywordRequest;
import com.serverManagement.server.management.request.keyword.SubKeywordRequest;
import com.serverManagement.server.management.request.keyword.UpdateKeywordName;
import com.serverManagement.server.management.request.keyword.UpdateSubKeywordName;
import com.serverManagement.server.management.response.keyword.KeywordResponse;
import com.serverManagement.server.management.response.keyword.SubKeywordResponse;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class KeywordService {

	@Autowired
	private KeywordDAO keywordDAO;
	@Autowired
	private SubKeywordDAO subKeywordDAO;
	@Autowired
	private AdminUserDAO adminUserDAO;

	private AdminUserEntity adminUserEntity;
	private KeywordEntity keywordEntity;

	// get list of keyword for option
	public ResponseEntity<List<String>> getKeyword() {

		List<String> keywordName = new ArrayList<String>();
		try {
			keywordName = keywordDAO.getKeywordList();
			if (keywordName.isEmpty() || keywordName == null) {
				throw new NullValueException("Not have any Keyword");
			} else {
				keywordName.sort((e1, e2) -> e1.compareTo(e2));
				return ResponseEntity.status(HttpStatus.OK).body(keywordName);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	// get list of sub-keyword under keyword if have
	public ResponseEntity<?> getSubKeywordOnKeyword(GetSubkeywordRequest keyword) {

		if (keyword == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("keyword is required to get sub keyword");
		} else {
			try {
				keywordEntity = keywordDAO.getSubKeywordList(keyword.getKeywordName().toLowerCase());
				if (keywordEntity == null || keywordEntity.getKeywordName() == null) {
					return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Not have any keyword with this Name");
				} else {
					KeywordResponse keywordResponse = new KeywordResponse();
					keywordResponse.setKeyword(keywordEntity.getKeywordName().toUpperCase());
					if (keywordEntity.getSubKeyword() != null && !keywordEntity.getSubKeyword().isEmpty()) {
						List<SubKeywordResponse> subKeywordList = keywordEntity.getSubKeyword().stream().map(subKey -> {
							SubKeywordResponse subKeyword = new SubKeywordResponse();
							subKeyword.setSubKeyword(subKey.getSubKeyword().toUpperCase());
							return subKeyword;
						}).collect(Collectors.toList());
						subKeywordList.sort((e1, e2) -> e1.getSubKeyword().compareTo(e2.getSubKeyword()));
						keywordResponse.setSubKeywordList(subKeywordList);
					}
					return ResponseEntity.status(HttpStatus.OK).body(keywordResponse);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
			}

		}
	}

	// add keyword and subKeyword first time only admin can perform this task
	public ResponseEntity<?> addKeyword(HttpServletRequest request, KeywordRequest keywordRequest) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not authenticated");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

					if (keywordRequest == null || keywordRequest.getKeyword() == null
							|| (keywordRequest.getKeyword() != null
									&& keywordRequest.getKeyword().trim().length() <= 0)) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Keyword is not detected, please add a new keyword and then try again hello"
										+ keywordRequest.getKeyword());
					} else {
						boolean isAvail = keywordDAO.existsByKeyword(keywordRequest.getKeyword().toLowerCase());
						if (isAvail) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("This keyword is already available");
						} else {
							KeywordEntity newKeyword = new KeywordEntity();
							newKeyword.setKeywordName(keywordRequest.getKeyword().toUpperCase());
							List<SubKeywordEntity> listOfSubkeyword = new ArrayList<SubKeywordEntity>();
							if (keywordRequest.getSubKeywordList() != null
									&& !keywordRequest.getSubKeywordList().isEmpty()) {
								Set<String> subKeywordSet = new HashSet<>();
								for (SubKeywordRequest subKey : keywordRequest.getSubKeywordList()) {
									if (subKey.getSubKeyword() == null || (subKey.getSubKeyword() != null
											&& subKey.getSubKeyword().trim().length() <= 0)) {
										continue;
									} else {
										subKeywordSet.add(subKey.getSubKeyword().toUpperCase());
									}
								}
//										keywordRequest.getSubKeywordList().stream().map(subKey -> {
//									if(subKey.getSubKeyword()!=null && subKey.getSubKeyword().trim().length()<=0) {
//										//if subKeyword null then fill blank
//									}else {
//									return subKey.getSubKeyword().toUpperCase();
//									}
//								}).collect(Collectors.toSet());
								for (String subKeywordName : subKeywordSet) {
									SubKeywordEntity subKeyword = new SubKeywordEntity();
									subKeyword.setSubKeyword(subKeywordName.toUpperCase());
									subKeyword.setKeywordRef(newKeyword);
									listOfSubkeyword.add(subKeyword);
								}
							}
							if (listOfSubkeyword != null || !listOfSubkeyword.isEmpty()) {
								newKeyword.setSubKeyword(listOfSubkeyword);
							}
							newKeyword = keywordDAO.save(newKeyword);
							if (newKeyword != null) {
								return ResponseEntity.status(HttpStatus.CREATED).body("Keyword Name added succesfully");
							} else {
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
										.body("Something went wrong");
							}
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can Add keywords");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	// add subKeyword if required to add more sub-keyword only admin can perform
	// this task
	public ResponseEntity<?> addSubKeyword(HttpServletRequest request, KeywordRequest keywordRequest) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not authenticated");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

					if (keywordRequest == null || keywordRequest.getKeyword() == null
							|| (keywordRequest.getKeyword() != null && keywordRequest.getKeyword().trim().length() <= 0)
							|| keywordRequest.getSubKeywordList().isEmpty()) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Keyword is not detected, please add a new kyword and then try again");
					} else {
						KeywordEntity keywordDetails = keywordDAO
								.getSubKeywordList(keywordRequest.getKeyword().toLowerCase());
						if (keywordDetails == null) {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("This keyword not added before, this is a new keyword");
						} else {
							//

							int count = 0;
							for (SubKeywordRequest subKey : keywordRequest.getSubKeywordList()) {
								if (subKey.getSubKeyword() == null || (subKey.getSubKeyword() != null
										&& subKey.getSubKeyword().trim().length() <= 0)) {
									continue;
								} else {
									boolean isAvailable = subKeywordDAO.existsBySubKeyword(keywordDetails,
											subKey.getSubKeyword().toLowerCase());
									if (!isAvailable) {
										SubKeywordEntity subKeyword = new SubKeywordEntity();
										subKeyword.setSubKeyword(subKey.getSubKeyword().toUpperCase());
										subKeyword.setKeywordRef(keywordDetails);
										subKeyword = subKeywordDAO.save(subKeyword);
										if (subKeyword != null) {
											count++;
										}
									}
								}
							}
							if (count > 0) {
								return ResponseEntity.status(HttpStatus.OK).body(count + " SubKeyword for "
										+ keywordDetails.getKeywordName().toUpperCase() + " is created successfully");
							} else {
								return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
										.body("All SubKeyword May be Duplicate, or have Empty");
							}
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can Add keywords");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	// update keyword name only update and only admin can perform this task
	public ResponseEntity<?> updateKeywordName(HttpServletRequest request, UpdateKeywordName updateKeywordName) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not authenticated");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

					if (updateKeywordName == null || updateKeywordName.getOldKeyword() == null
							|| updateKeywordName.getNewKeyword() == null
							|| (updateKeywordName.getOldKeyword() != null
									&& updateKeywordName.getOldKeyword().trim().length() <= 0)
							|| (updateKeywordName.getNewKeyword() != null
									&& updateKeywordName.getNewKeyword().trim().length() <= 0)) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Keyword is not detected, please add a new kyword and then try again");
					} else {
						keywordEntity = keywordDAO.getSubKeywordList(updateKeywordName.getOldKeyword().toLowerCase());
						if (keywordEntity != null) {
							boolean isAvail = keywordDAO
									.existsByKeyword(updateKeywordName.getNewKeyword().toLowerCase());
							if (!isAvail) {
								keywordEntity.setKeywordName(updateKeywordName.getNewKeyword().toUpperCase());
								keywordEntity = keywordDAO.save(keywordEntity);
								if (keywordEntity != null) {
									return ResponseEntity.status(HttpStatus.CREATED)
											.body(updateKeywordName.getOldKeyword().toUpperCase()
													+ " is updated with new keyword :"
													+ updateKeywordName.getNewKeyword().toUpperCase());
								} else {
									return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
											.body("Something went wrong");
								}
							} else {
								return ResponseEntity.status(HttpStatus.CONFLICT)
										.body("This keyword is already available");
							}
						} else {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body(updateKeywordName.getOldKeyword().toUpperCase()
											+ ", This keyword not registered before");
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Admin can update keyword name");
				}
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
		}
	}

	// update sub-keyword name under keyword only admin can perform this task
	public ResponseEntity<?> updateSubKeyWordName(HttpServletRequest request,
			UpdateSubKeywordName updateSubKeywordName) {

		String loggedInEmail = null;
		try {
			loggedInEmail = request.getUserPrincipal().getName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
		}
		try {
			adminUserEntity = adminUserDAO.findByEmail(loggedInEmail);
			if (adminUserEntity == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not authenticated");
			} else {
				if (adminUserEntity.getRoleModel().getRoleName().toLowerCase().equals("admin")) {

					if (updateSubKeywordName == null || updateSubKeywordName.getKeywordName() == null
							|| updateSubKeywordName.getOldSubKeyword() == null
							|| updateSubKeywordName.getUpdateSubKeyword() == null
							|| (updateSubKeywordName.getUpdateSubKeyword() != null
									&& updateSubKeywordName.getUpdateSubKeyword().trim().length() <= 0)) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								.body("Keyword is not detected, please add a new kyword and then try again");
					} else {
						KeywordEntity keywordDetails = keywordDAO
								.getSubKeywordList(updateSubKeywordName.getKeywordName().toLowerCase());
						if (keywordDetails != null) {
							//
							SubKeywordEntity existSubKeyword = subKeywordDAO.getSpecificSubKeyword(keywordDetails,
									updateSubKeywordName.getOldSubKeyword().toLowerCase());
							if (existSubKeyword != null) {

								SubKeywordEntity newSubKeyword = subKeywordDAO.getSpecificSubKeyword(keywordDetails,
										updateSubKeywordName.getUpdateSubKeyword().toLowerCase());
								if(newSubKeyword ==null) {
								existSubKeyword.setSubKeyword(updateSubKeywordName.getUpdateSubKeyword().toUpperCase());
								existSubKeyword = subKeywordDAO.save(existSubKeyword);
								if (existSubKeyword != null) {
									return ResponseEntity.status(HttpStatus.OK).body("subKeyword updated successfully");
								} else {
									return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
											.body("Something went wrong");
								}
								}else { 
									return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This new sub Keyword already register under this keyword");
								}
									
							} else {
								return ResponseEntity.status(HttpStatus.BAD_REQUEST)
										.body("this subKeyword not register under this keyword");
							}
						} else {
							return ResponseEntity.status(HttpStatus.BAD_REQUEST)
									.body("This keyword not added before, this is a new keyword");
						}
					}
				} else {
					return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admin can Add keywords");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal server error");
		}
	}

}
