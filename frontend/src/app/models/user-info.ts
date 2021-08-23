import { Paging } from "./paging";

export interface UserInfo {
  /** id */
  id: number;
  /** username */
  username: string;
  /** role */
  role: string;
  /** whether the user is disabled, 0-normal, 1-disabled */
  isDisabled: number;
}

export interface FetchUserInfoResp {
  fileInfoList: UserInfo[];
  pagingVo: Paging;
}

export enum UserIsDisabledEnum {
  /**
   * User is in normal state
   */
  NORMAL = 0,

  /**
   * User is disabled
   */
  IS_DISABLED = 1,
}

export enum UserRoleEnum {
  /** Administrator */
  ADMIN = "admin",

  /** Normal user */
  USER = "user",

  /** Guest */
  GUEST = "guest",
}

export interface UserIsDisabledOption {
  name: string;
  value: number;
}

export const USER_IS_DISABLED_OPTIONS: UserIsDisabledOption[] = [
  { name: "normal", value: UserIsDisabledEnum.NORMAL },
  { name: "disabled", value: UserIsDisabledEnum.IS_DISABLED },
];

export interface UserRoleOption {
  name: string;
  value: string;
}

export const USER_ROLE_OPTIONS: UserRoleOption[] = [
  { name: "admin", value: UserRoleEnum.ADMIN },
  { name: "user", value: UserRoleEnum.USER },
  { name: "guest", value: UserRoleEnum.GUEST },
];

/**
 * Parameters for adding a new user
 */
export interface AddUserParam {
  /** username */
  username: string;
  /** password */
  password: string;
  /** user role */
  userRole: string;
}

/**
 * Parameters for changing password
 */
export interface ChangePasswordParam {
  /**
   * Previous password
   */
  prevPassword: string;

  /**
   * New password
   */
  newPassword: string;
}

/**
 * Empty object with all properties being null values
 */
export function emptyChangePasswordParam(): ChangePasswordParam {
  return {
    prevPassword: null,
    newPassword: null,
  };
}

/**
 * Parameters for fetching user info
 */
export interface FetchUserInfoParam {
  /** username  */
  username: string;

  /**
   * role
   */
  role: UserRoleEnum | string;

  /**
   * is user disabled
   */
  isDisabled: UserIsDisabledEnum | number;

  /**
   * paging param
   */
  pagingVo: Paging;
}

export function emptyFetchUserInfoParam(): FetchUserInfoParam {
  return {
    username: null,
    role: null,
    isDisabled: null,
    pagingVo: null,
  };
}
