/** 
 * @file strings.h
 *
 * @copydoc strings.c
 */
#ifndef _STRINGS_H_
#define _STRINGS_H_

/* Strings for variables */
extern const __flash char *state_names[];

/* Strings for error messages. */
extern const __flash char str_msg_not_found_e[];
extern const __flash char str_rsp_system_address_get_e[];
extern const __flash char str_rsp_set_adv_param_e[];
extern const __flash char str_rsp_set_adv_data_e[];
extern const __flash char str_rsp_set_gap_mode_e[];
extern const __flash char str_rsp_att_write_e[];
extern const __flash char str_inv_nonce_e[];
extern const __flash char str_inv_auth_dec_e[];
extern const __flash char str_inv_auth_enc_e[];
extern const __flash char str_process_packet_e[];
extern const __flash char str_uart_tx_e[];

/* Strings for general information */
extern const __flash char str_reset_i[];
extern const __flash char str_power_down_i[];
extern const __flash char str_wake_up_i[];
extern const __flash char str_locking_i[];
extern const __flash char str_locked_i[];
extern const __flash char str_unlocking_i[];
extern const __flash char str_unlocked_i[];
extern const __flash char str_program_start_i[];
extern const __flash char str_low_battery_i[];

#endif
