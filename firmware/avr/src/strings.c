/** 
 * @file strings.c
 *
 * Contains all strings used in the program. They are stored in flash to save
 * ram.
 */
#include "strings.h"

/* Strings for variables */
/* Friendly names for states. */
const __flash char str_reset[]                  = "reset";
const __flash char str_ble_boot_w[]             = "ble_boot_w";
const __flash char str_ble_boot[]               = "ble_boot";
const __flash char str_system_address_get_w[]   = "system_address_get_w";
const __flash char str_system_address_get[]     = "system_address_get";
const __flash char str_ble_adv_param_set_w[]    = "ble_adv_param_set_w";
const __flash char str_ble_adv_param_set[]      = "ble_adv_param_set";
const __flash char str_ble_adv_data_set_w[]     = "ble_adv_data_set_w";
const __flash char str_ble_adv_data_set[]       = "ble_adv_data_set";
const __flash char str_ble_adv_sr_data_set_w[]  = "ble_adv_sr_data_set_w";
const __flash char str_ble_adv_sr_data_set[]    = "ble_adv_sr_data_set";
const __flash char str_advertising_w[]          = "advertising_w";
const __flash char str_advertising[]            = "advertising";
const __flash char str_adv_sleep_w[]            = "adv_sleep_w";
const __flash char str_adv_sleep[]              = "adv_sleep";
const __flash char str_connected[]              = "connected";
const __flash char str_ticket[]                 = "ticket";
const __flash char str_request_w[]              = "request_w";
const __flash char str_request[]                = "request";
const __flash char str_disconnected[]           = "disconnected";
const __flash char *state_names[] = {
    str_reset,
    str_ble_boot_w,
    str_ble_boot,
    str_system_address_get_w,
    str_system_address_get,
    str_ble_adv_param_set_w,
    str_ble_adv_param_set,
    str_ble_adv_data_set_w,
    str_ble_adv_data_set,
    str_ble_adv_sr_data_set_w,
    str_ble_adv_sr_data_set,
    str_advertising_w,
    str_advertising,
    str_adv_sleep_w,
    str_adv_sleep,
    str_connected,
    str_ticket,
    str_request_w,
    str_request,
    str_disconnected
};

/* Strings for error messages. */
const __flash char str_msg_not_found_e[]          = "error: msg not found";
const __flash char str_rsp_system_address_get_e[] = "error: rsp system address get";
const __flash char str_rsp_set_adv_param_e[]      = "error: rsp set adv param";
const __flash char str_rsp_set_adv_data_e[]       = "error: rsp set adv data";
const __flash char str_rsp_set_gap_mode_e[]       = "error: rsp set gap mode";
const __flash char str_rsp_att_write_e[]          = "error: att write";
const __flash char str_inv_nonce_e[]              = "error: invalid nonce";
const __flash char str_inv_auth_dec_e[]           = "error: invalid auth dec";
const __flash char str_inv_auth_enc_e[]           = "error: invalid auth enc";
const __flash char str_process_packet_e[]         = "error: read api";
const __flash char str_uart_tx_e[]                = "error: uart_tx timeout";

/* Strings for general information */
const __flash char str_reset_i[]                = "reset in 1 sec";
const __flash char str_power_down_i[]           = "power down";
const __flash char str_wake_up_i[]              = "wake up";
const __flash char str_locking_i[]              = "locking";
const __flash char str_locked_i[]               = "locked";
const __flash char str_unlocking_i[]            = "unlocking";
const __flash char str_unlocked_i[]             = "unlocked";
const __flash char str_program_start_i[]        = "program start";
const __flash char str_low_battery_i[]          = "low battery";
