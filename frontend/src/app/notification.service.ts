import { Injectable } from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  constructor(private snackBar: MatSnackBar) {}

  /**
   * Toast a message with given action
   * @param action if not specified, the action is by default "Okay"
   * @param msg msg
   * @param duration if not specified, the duration is by default 1000 milliseconds
   */
  public toast(msg: string, duration: number = 1000, action: string = "Okay") {
    if (duration <= 0) {
      this.snackBar.open(msg, action);
    } else {
      this.snackBar.open(msg, action, {
        duration: duration,
      });
    }
  }
}
