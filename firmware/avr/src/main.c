/**
 * @file main.c
 *
 * Main file. 
 *
 * This is the main file where the main function and some helper functions are
 * defined.
 */

#include <avr/interrupt.h>
#include <avr/sleep.h>
#include <avr/wdt.h>
#include <string.h>
#include <util/delay.h>
#include "attributes.h"
#include "button.h"
#include "cmd_def.h"
#include "crypto.h"
#include "motor_control.h"
#include "requests.h"
#include "responses.h"
#include "uart.h"

#define PRINTS 0

#if PRINTS
    #include "spi.h"
    #include "strings.h"
#endif

#define BAUD 9600           /* Baud rate for UART communication  */
#define UART_TIMEOUT 100    /* Timeout (in millisec) for UART communication */

/* TODO: response handlers for when BT module says that command was not
 * sucessfully executed. */

/* Maximum BLE message length */
#define MAX_MESSAGE_LEN TICKET_LEN
#define TICKET_LEN 72       /* 24 nonce + 16 auth + 32 key */
#define REQUEST_LEN 41      /* 24 nonce + 16 auth + 1 request */
#define RESPONSE_LEN 41     /* 24 nonce + 16 auth + 1 response */

/**************************** GLOBAL VARIABLES ** ****************************/

/**
 * States in which the product can be.
 *
 * Some states (e.g. ADVERTISE_POWER_DOWN) are ommited because because they
 * aren't needed in the code. The states with suffix _W are states in which the
 * MCU is waiting for an event, generally a message from the BLE.  */
enum states {
    RESET,
    BLE_BOOT_W,
    BLE_BOOT,
    SYSTEM_ADDRESS_GET_W,
    SYSTEM_ADDRESS_GET,
    ADV_PARAM_SET_W,
    ADV_PARAM_SET,
    ADV_DATA_SET_W,
    ADV_DATA_SET,
    ADV_SR_DATA_SET_W,
    ADV_SR_DATA_SET,
    ADVERTISING_W,
    ADVERTISING,
    ADV_SLEEP_W,
    ADV_SLEEP,
    CONNECTED,
    TICKET,
    REQUEST_W,
    REQUEST,
    DISCONNECTED
} state = RESET;

/**
 * Buffer for incomming UART messages.
 */
struct msg_buffer {
    uint8_t len;
    uint8_t data[MAX_MESSAGE_LEN];
} msg_buf = {0, {0}};

uint8_t system_address[6];

/**
 * Buffer for outgoing ble_attributes_write message.
 */
struct attributes_write_buffer {
    uint8_t section_sent;
    uint8_t data[RESPONSE_LEN];
} att_write_buffer = {0, {0}};

/*************************** WATCHDOG TIMER/RESET ****************************/

/**
 * Initialize watchdog timer.
 *
 * The watchdog timer is initially disabled. If the watchdog is accidentally 
 * enabled, for example by a runaway pointer or brown-out condition, the device
 * will be reset and the watchdog timer will stay enabled. If the code is not
 * set up to handle the watchdog, this might lead to an eternal loop of 
 * time-out resets.
 */
void wdt_init()
{
    MCUSR = 0;
    wdt_disable();
}

/**
 * Software reset the MCU.
 *
 * Reset is done using watchdog timer. This removes all contents of SRAM.
 */
void soft_reset()
{
    cli();
#if PRINTS
    spi_print_flash_string(str_reset_i);
#endif
    wdt_enable(WDTO_15MS);
    while (1);
}

/************************* SEND/PROCESS API PACKETS **************************/

/**
 * Send BGAPI packets to the bluetooth module over UART.
 *
 * This function should never be called directly. Instead, the function pointer
 * bglib_output is set to point to this function in main() so that everytime 
 * a BGLib (see cmd_def.c) command is executed it will use this function as its 
 * output in order to transmit packets.
 *
 * @param[in]   len1    Length of data1.
 * @param[in]   data1   First part of the data to be transmitted.
 * @param[in]   len2    Length of data2.
 * @param[in]   data2   Second part of the data to be transmitted.
 */
void ble_send_packet(uint8 len1, uint8* data1, uint16 len2, uint8* data2)
{
    /* Transmit complete packet via UART. */
    if (uart_tx(len1, data1, len2, data2, UART_TIMEOUT)) {
#if PRINTS
        spi_print_flash_string(str_uart_tx_e);
#endif
        soft_reset();
    }
}

/**
 * Read the BGAPI packet that is received over UART and process it.
 *
 * @return -1 if a timeout occurs, there is nothing ready to be received.
 * @return  0 if the packet is processed correctly.
 * @return  1 if there is an error retreiving all the data.
 * @return  2 if there is an error processing the packet.
 */
int ble_process_packet()
{
    int8_t rrx;
    unsigned char data[MAX_PAYLOAD];
    struct ble_header hdr;
    const __flash struct ble_msg *msg;

    rrx = uart_rx(sizeof(hdr), (unsigned char*) &hdr, UART_TIMEOUT);
    if (rrx) {
        return rrx; /* Timeout: rrx=-1, Data receive failed: rrx=1 */
    }
    if (hdr.lolen) {
        if(uart_rx(hdr.lolen, data, UART_TIMEOUT)) {
            return 1; /* Data receive failed */
        }
    }
    msg = ble_get_msg_hdr(hdr);
    if (!msg) {
#if PRINTS
        spi_print_flash_string(str_msg_not_found_e);
#endif
        return 2; /* Msg receive failed */
    }

    /* call the appropriate handler function with any payload data
     * (this is what triggers the ble_evt_* and ble_rsp_* functions) */
    /* ((ble_cmd_handler) pgm_read_word(&msg->handler))(data); */
    msg->handler(data);
    return 0;
}

/*************************** AUXILIARY FUNCTIONS *****************************/

/**
 * Reset BLE.
 */
void ble_hard_reset()
{
    PORTD |= (1 << PIND3);
    _delay_ms(100);
    PORTD &= ~(1 << PIND3);
}

/**
 * Initialize the BLE hard reset pin.
 */
void ble_hard_reset_init()
{
    DDRD |= (1 < PIND3);
    PORTD &= ~(1 << PIND3);
}

void battery_monitor_enable() {
    ACSR &= ~(1 << ACD);
}

void battery_monitor_disable() {
    ACSR |= (1 << ACD);
}

/**
 * Initialize battery monitor.
 */
void battery_monitor_init() {
    battery_monitor_enable();
    /* Disable ADC, enable comparator multiplexer */
    ADCSRA &= ~(1 << ADEN);
    ADCSRB |= (1 << ACME);
    /* ADC7 as negative input to comparator, 1.1V as reference */
    ADMUX |= (1 << MUX2 | 1 << MUX1 | 1 << MUX0);
    /* Set bandgap (internal) reference as positive input to comparator */
    ACSR |= (1 << ACBG);
}

/**
 * Returns 1 if battery voltage is too low, else 0.
 */
uint8_t battery_monitor_alert() {
    return bit_is_set(ACSR, ACO);
}

/***************************** SLEEP FUNCTIONS *******************************/

/**
 * Wake up BLE.
 *
 * When waking up BLE, only start sending new data over UART when
 * ble_evt_hardware_io_port_status has been received. This might not be
 * necessary when using flow control.
 */
void ble_wakeup()
{
    DDRC |= 1<<PINC3;
    PORTC |= 1<<PINC3;
}

/**
 * Allow BLE to go to sleep.
 */
void ble_sleep()
{
    DDRC |= 1<<PINC3;
    PORTC &= ~(1<<PINC3);
}

/**
 * Enables interrupts on wakeup pin.
 *
 * Interrupts get triggerd when INT0 is low.
 */
void wakeup_pin_enable()
{
    EIMSK = 1<<INT0;
}

/**
 * Disables interrupts on wakeup pin.
 */
void wakeup_pin_disable()
{
    EIMSK &= ~(1<<INT0);
}

/**
 * Power down the device.
 *
 * Turns off all interrupt driven events, enables wakeup pin and enters power
 * down mode. When the wakeup pin is triggered, it wakes up and reenables all 
 * interrupt driven events.
 */
void power_down()
{
    /* Turn off interrupt driven events */
    uart_not_ready_to_receive();
    /*uart_disable();*/ /* Should be here, but messes up BLE pin changes */

    /* Put other ICs to sleep */
    ble_sleep();

    /* Disable battery monitor to minimize power consumption. */
    battery_monitor_disable();

#if PRINTS
    spi_print_flash_string(str_power_down_i);
#endif

    /********** Do not touch **********/
    /* http://www.nongnu.org/avr-libc/user-manual/group__avr__sleep.html */
    set_sleep_mode(SLEEP_MODE_PWR_DOWN);
    cli();
    if (PIND & (1<<PIND2)) { /* Don't sleep if wakeup is enabled (low) */
        /* Enable wake up pin interrupt */
        wakeup_pin_enable();
        sleep_enable();
        /* sleep_bod_disable(); */
        sei();
        sleep_cpu();
        sleep_disable();
    }
    sei();
    /********** Wake up **********/
    /* Interupt on pin INT0 triggered. */
#if PRINTS
    spi_print_flash_string(str_wake_up_i);
#endif

    battery_monitor_enable();

    /* Wake up other ICs */
    ble_wakeup();

    /* Enable interrupt driven events */
    /*uart_enable();*/
    uart_ready_to_receive();

}

/***************************** PROTOCOL SUPPORT ******************************/

/**
 * Resets the rxdata buffer.
 */
void reset_msg_buffer() {
    msg_buf.len = 0;
}

/**
 * Compute if a is greater than b.
 *
 * Both arrays must be of same length.
 * @param[in] a         first array
 * @param[in] b         second array
 * @param[in] length    length of the arrays
 * @return 1 when a is greater than b, 0 otherwise
 */
uint8_t is_greater_than(uint8_t *a, uint8_t *b, uint8_t length) {
    uint8_t i;
    for(i = 0; i < length; i++) {
        if(a[i] < b[i]) {
            return 0;
        }
        else if(a[i] > b[i]) {
            return 1;
        }
    }
    return 0; /* nonce is equal to session nonce */
#if 0 /* Recursive implementation */
    if (a[0] > b[0]) return 1;
    if (a[0] < b[0]) return 0;
    if (length > 1) { /* a[0] == b[0] */
        return is_greater_than(a+1, b+1, length-1);
    } else {
        return 0; /* a == b */
    }
#endif
}

/**
 * Returns whether the new nonce is valid or not.
 *
 * @param[in] nonce         new nonce.
 * @param[in] master_nonce  last used nonce.
 * @return 1 if nonce is valid, 0 otherwise.
 */
uint8_t is_valid_ticket_nonce(uint8_t *nonce, uint8_t *master_nonce) {
    return is_greater_than(nonce, master_nonce, NONCE_LEN);
}

/**
 * Returns whether the nonce is valid or not.
 *
 * @param[in] nonce the nonce to check.
 * @return 1 if nonce is valid, 0 otherwise.
 */
uint8_t is_valid_session_nonce(uint8_t *nonce) {
    return is_greater_than(nonce, session_nonce, NONCE_LEN);
}    

/**
 * Increment byte array by one.
 *
 * @param[in] array     array to increment
 * @param[in] length    length of the array
 */
void increment_array(uint8_t *array, uint8_t length) {
    uint8_t i;
    for(i = length-1; i >= 0; i--) {
        if(array[i] == 255) {
            array[i] = 0;
        }
        else {
            array[i]++;
            return;
        }
    }
#if 0 /* Recurive implementation  */
    /* Increment Last element */
    array[length-1]++;
    
    /* If last byte is equal to zero and not the only byte left, increment
     * the next more significant byte. */
    if (array[length-1] == 0 && length > 1) {
        increment_array(array, length-1);
    }
#endif 
}

/***************************** RESPONSE HANDLERS *****************************/

/**
 * Get system address response handler.
 *
 * Handles the received response to the command 
 * ble_cmd_system_address_get() that was sent to the bluetooth module. It
 * does nothing when the response indicates that the command was successful and 
 * nothing when the command was not successful.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_rsp_system_address_get(const struct ble_msg_system_address_get_rsp_t * msg) {
    int i = 0;
    state = SYSTEM_ADDRESS_GET;
    for(i = 0; i < 6; i++) {
        system_address[i] = msg->address.addr[i];
    }
}

/**
 * Set advertising parameters response handler.
 *
 * Handles the received response to the command 
 * ble_cmd_gap_set_adv_parameters() that was sent to the bluetooth module. It
 * does nothing when the response indicates that the command was successful and 
 * nothing when the command was not successful.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_rsp_gap_set_adv_parameters(const struct ble_msg_gap_set_adv_parameters_rsp_t *msg)
{
    state = ADV_PARAM_SET;
#if PRINTS
    if (msg->result) {
        spi_print_flash_string(str_rsp_set_adv_param_e);
    }
#endif
}

/**
 * Set advertising data response handler.
 *
 * Handles the received response to the command 
 * ble_cmd_gap_set_adv_data() that was sent to the bluetooth module. It
 * does nothing when the response indicates that the command was successful and 
 * nothing when the command was not successful.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_rsp_gap_set_adv_data(const struct ble_msg_gap_set_adv_data_rsp_t *msg)
{
    if(state == ADV_DATA_SET_W) {
        state = ADV_DATA_SET;
    }
    else if(state == ADV_SR_DATA_SET_W) {
        state = ADV_SR_DATA_SET;
    }
#if PRINTS
    if (msg->result) {
        spi_print_flash_string(str_rsp_set_adv_data_e);
    }
#endif
}

/**
 * Set mode response handler.
 *
 * Handles the received response to the command ble_cmd_gap_set_mode() that 
 * was sent to the bluetooth module. It does nothing when the response 
 * indicates that the command was successful and nothing when the command was 
 * not successful.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_rsp_gap_set_mode(const struct ble_msg_gap_set_mode_rsp_t *msg)
{
    state = ADVERTISING;
#if PRINTS
    if (msg->result) {
        spi_print_flash_string(str_rsp_set_gap_mode_e);
    }
#endif
}

/**
 * Attributes write response handler.
 *
 * Handles the received response to the command ble_cmd_attributes_write() that
 * was sent to the bluetooth module. Max data that can be sent in one
 * ble_cmd_attributes_write command is 20 bytes. Since responses are
 * RESPONSE_LEN=41 bytes, this must be done in three writes. The next
 * ble_cmd_attributes_write should only be sent after a
 * ble_rsp_attributes_write has been received.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_rsp_attributes_write(const struct ble_msg_attributes_write_rsp_t *msg)
{
    if (msg->result) {
#if PRINTS
        spi_print_flash_string(str_rsp_att_write_e);
#endif
        return;
    }
    if (att_write_buffer.section_sent == 0) {
        ble_cmd_attributes_write(BG_ATT_c_lock_transmit,0,20,att_write_buffer.data + 20);
        att_write_buffer.section_sent++;
    } else if (att_write_buffer.section_sent == 1) {
        ble_cmd_attributes_write(BG_ATT_c_lock_transmit,0,1,att_write_buffer.data + 40);
        att_write_buffer.section_sent++;
    }
}

/****************************** EVENT HANDLERS *******************************/

/**
 * System boot event handler.
 *
 * When the bluetooth module is booted up commands to start advertising are
 * sent to the bluetooth module.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_evt_system_boot(const struct ble_msg_system_boot_evt_t *msg) {
    state = BLE_BOOT;
}

/**
 * Connection status event handler.
 *
 * When a new connection to the bluetooth module is made the MCU does nothing.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_evt_connection_status(const struct ble_msg_connection_status_evt_t *msg)
{
    /* flags = 0101 (see page 90 BLE API ref) */
    if(msg->flags & 5) { 
        state = CONNECTED;
    }
}

/**
 * Connection disconnected event handler.
 *
 * When the bluetooth module loses a connection (disconnects) commands are sent
 * to start advertising. The module doesn't automatically start advertising
 * when disconnected.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_evt_connection_disconnected(const struct ble_msg_connection_disconnected_evt_t *msg)
{
    state = DISCONNECTED;
}

/**
 * Wake up event handler.
 *
 * When the microcontroller wakes up the BLE by setting the wake up pin high,
 * the BLE triggers this event. This event must be received before sending any
 * UART data.
 */
void ble_evt_hardware_io_port_status(const struct ble_msg_hardware_io_port_status_evt_t *msg)
{
#if 0
    /* Ble wakeup pin on BLE is P1_3 */
    if (msg->port == 1 && msg->irq == 0x08 && (msg->state & 0x08)) { /* Wake up */
        /* Must wait until this message is received before sending data to BLE.
         * This will never happen, because nothing is sent to ble when it is
         * sleeping since mcu is waiting for a connection event. In addition
         * there is flow control to prevent sending data to BLE when it is not
         * ready. */
    }
#endif
}


/**
 * Attributes value event handler.
 *
 * Checks what attribute is written and handles accordingly. When 0/1 is
 * written to the attribute BG_ATT_c_lock_action the door wil unlock/lock.
 *
 * @param[in]   msg The BGAPI message received from the bluetooth module.
 */
void ble_evt_attributes_value(const struct ble_msg_attributes_value_evt_t * msg) {
    uint8_t packet_len = msg->value.len;

    if (msg->handle == BG_ATT_c_lock_receive) {
        /* Update rx buffer */
        memcpy(msg_buf.data + msg_buf.len, msg->value.data, packet_len);
        msg_buf.len += packet_len;

        if (state == CONNECTED && msg_buf.len == TICKET_LEN) {
            state = TICKET;
        } else if (state == REQUEST_W && msg_buf.len == REQUEST_LEN) {
            state = REQUEST;
        }
    }
}

/****************************** FSM FUNCTIONS ********************************/

void set_adv_parameters() {
    ble_cmd_gap_set_adv_parameters(320,350,7);
}

void process_adv_param_set() {
        uint8_t adv_data[21] = {0x02, 0x01, 0x06, 0x11, 0x07, 0x5b, 0xd4, 0xe8, 0x2a, 0xee, 0xca, 0x2a, 0x88, 0x97, 0x45, 0x10, 0xd5, 0x27, 0xf9, 0x2c, 0x30};
        ble_cmd_gap_set_adv_data(0, 21, adv_data);
        state = ADV_DATA_SET_W;
}

void process_adv_data_set() {
        uint8_t sr_data[26] = {0};
        sr_data[0] = 18;
        sr_data[1] = 0xFF;
        sr_data[2] = (system_address[5]/16)+48+((system_address[5]/16)/10*39);
        sr_data[3] = (system_address[5]&0x0f)+48+((system_address[5]&0x0f)/10*39);
        sr_data[4] = 0x3a;
        sr_data[5] = (system_address[4]/16)+48+((system_address[4]/16)/10*39);
        sr_data[6] = (system_address[4]&0x0f)+48+((system_address[4]&0x0f)/10*39);
        sr_data[7] = 0x3a;
        sr_data[8] = (system_address[3]/16)+48+((system_address[3]/16)/10*39);
        sr_data[9] = (system_address[3]&0x0f)+48+((system_address[3]&0x0f)/10*39);
        sr_data[10] = 0x3a;
        sr_data[11] = (system_address[2]/16)+48+((system_address[2]/16)/10*39);
        sr_data[12] = (system_address[2]&0x0f)+48+((system_address[2]&0x0f)/10*39);
        sr_data[13] = 0x3a;
        sr_data[14] = (system_address[1]/16)+48+((system_address[1]/16)/10*39);
        sr_data[15] = (system_address[1]&0x0f)+48+((system_address[1]&0x0f)/10*39);
        sr_data[16] = 0x3a;
        sr_data[17] = (system_address[0]/16)+48+((system_address[0]/16)/10*39);
        sr_data[18] = (system_address[0]&0x0f)+48+((system_address[0]&0x0f)/10*39);
        sr_data[19] = 6;
        sr_data[20] = 9;
        sr_data[21] = 'e';
        sr_data[22] = 's';
        sr_data[23] = 'l';
        sr_data[24] = 'o';
        sr_data[25] = 'q';

        ble_cmd_gap_set_adv_data(1, 26, sr_data);
        state = ADV_SR_DATA_SET_W;
}

/**
 * Process the ticket message.
 *
 * This function should be called after the ticket has completely been
 * received.
 *
 * @return 0 if ticket sucessfully processed, 1 if nonce is invalid, 2 if authenticated decryption failed, 3 if authenticated encryption failed.
 */
int8_t process_ticket_msg() {
    uint8_t *nonce = msg_buf.data;
    uint8_t master_nonce[NONCE_LEN];
    uint8_t master_key[KEY_LEN];
    uint8_t c[KEY_LEN+AUTH_LEN] = {0};
    uint8_t m[KEY_LEN] = {0};
    uint8_t rsp[RESPONSE_LEN] = {INVALID_REQUEST};
    uint8_t rsp_len = 1;

    eeprom_read_block(master_nonce, master_nonce_eeprom, NONCE_LEN);
    if (!is_valid_ticket_nonce(nonce, master_nonce)) {
#if PRINTS
        spi_print_flash_string(str_inv_nonce_e);
#endif
        return 1;
    }
    eeprom_update_block(nonce, master_nonce_eeprom, NONCE_LEN);

    /* Set nonce as session nonce. */
    memcpy(master_nonce, nonce, NONCE_LEN);

    eeprom_read_block(master_key, master_key_eeprom, KEY_LEN);
    if (crypto_auth_decrypt(m, nonce+NONCE_LEN, TICKET_LEN-NONCE_LEN, nonce, master_key)) {
#if PRINTS
        spi_print_flash_string(str_inv_auth_dec_e);
#endif
        return 2;
    }

    /* Store session key and reinitialize session nonce to 0. */
    memcpy(session_key, m, KEY_LEN);
    memset(session_nonce, 0, NONCE_LEN);

    /* Send response */

    if(battery_monitor_alert()) {
#if PRINTS
        spi_print_flash_string(str_low_battery_i);
#endif
        m[0] = TICKET_RCV_SUCCESS_LOW_BAT;
    } else {
        m[0] = TICKET_RCV_SUCCESS;
    }
    /* Increment session nonce.  */
    increment_array(session_nonce, NONCE_LEN);
    if (crypto_auth_encrypt(c, m, 1, session_nonce, session_key)) {
#if PRINTS
        spi_print_flash_string(str_inv_auth_enc_e);
#endif
        return 3;
    }

    /* Response consists of: nonce | AE(m) */
    memcpy(rsp, session_nonce, NONCE_LEN);
    memcpy(rsp+NONCE_LEN, c, AUTH_LEN + rsp_len);

    reset_msg_buffer();
    state = REQUEST_W;

    memcpy(att_write_buffer.data, rsp, RESPONSE_LEN);
    ble_cmd_attributes_write(BG_ATT_c_lock_transmit,0,20,att_write_buffer.data);
    att_write_buffer.section_sent = 0;
    return 0;
}

/**
 * Process the request.
 *
 * Should be called after the user has authenticated himself and the request
 * has been received.
 *
 * @return 0 if request sucessfully processed, 1 if nonce is invalid, 2 if
 * authenticated decryption failed, 3 if authenticated encryption failed.
 */
int8_t process_request_msg() {
    uint8_t *nonce = msg_buf.data;
    uint8_t c[1+AUTH_LEN] = {0};
    uint8_t m[1] = {0};
    uint8_t rsp[RESPONSE_LEN] = {INVALID_REQUEST};
    uint8_t rsp_len = 1;
    uint8_t request;
    RSP_CODE rsp_code = INVALID_REQUEST;

    state = REQUEST_W;
    if (!is_valid_session_nonce(nonce)) {
#if PRINTS
        spi_print_flash_string(str_inv_nonce_e);
#endif
        return 1;
    }

    /* Set nonce as session nonce. */
    memcpy(session_nonce, nonce, NONCE_LEN);

    if (crypto_auth_decrypt(m, nonce+NONCE_LEN, REQUEST_LEN-NONCE_LEN, nonce, session_key)) {
#if PRINTS
        spi_print_flash_string(str_inv_auth_dec_e);
#endif
        return 2;
    }

    request = m[0];
    switch (request) {
        case ROTATE_CW:
#if PRINTS
            spi_print_flash_string(str_locking_i);
#endif
            rsp_code = rotate_motor_cw();
#if PRINTS
            spi_print_flash_string(str_locked_i);
#endif
            break;
        case ROTATE_CCW:
#if PRINTS
            spi_print_flash_string(str_unlocking_i);
#endif
            rsp_code = rotate_motor_ccw();
#if PRINTS
            spi_print_flash_string(str_unlocked_i);
#endif
            break;
        default:
            rsp_code = INVALID_REQUEST;
            break;
    }

    m[0] = rsp_code;

    /* Increment session nonce.  */
    increment_array(session_nonce, NONCE_LEN);

    if (crypto_auth_encrypt(c, m, 1, session_nonce, session_key)) {
#if PRINTS
        spi_print_flash_string(str_inv_auth_enc_e);
#endif
        return 3;
    }
    memcpy(rsp, session_nonce, NONCE_LEN);
    memcpy(rsp+NONCE_LEN, c, AUTH_LEN + rsp_len);
    reset_msg_buffer();

    memcpy(att_write_buffer.data, rsp, RESPONSE_LEN);
    ble_cmd_attributes_write(BG_ATT_c_lock_transmit,0,20,att_write_buffer.data);
    att_write_buffer.section_sent = 0;
    return 0;
}


/* Function pointers probably better but requires wrappers so that all
 * functions have the same (void) argument and the same (void) return type.  */
/* TODO maybe add default case, that if waiting takes to long, MCU resets. */
/**
 * Finite State Machine that implements the business logic.
 *
 * FSM that reacts based on the state it is currently in. States get updated
 * by (external) events
 */
void fsm_process() {
    switch (state) {
        case BLE_BOOT:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            ble_cmd_system_address_get();
            state = SYSTEM_ADDRESS_GET_W;
            break;
        case SYSTEM_ADDRESS_GET:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            set_adv_parameters();
            state = ADV_PARAM_SET_W;
            break; 
        case ADV_PARAM_SET:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            process_adv_param_set();
            break;
        case ADV_DATA_SET:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            process_adv_data_set();
            break;
        case ADV_SR_DATA_SET:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            ble_cmd_gap_set_mode(gap_user_data,gap_undirected_connectable);
            state = ADVERTISING_W;
            break;
        case ADVERTISING:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            power_down();
            break;
        case TICKET:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            if (process_ticket_msg()) {
                reset_msg_buffer();
                state = CONNECTED;
            }
            break;
        case REQUEST:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            if (process_request_msg()) {
                reset_msg_buffer();
            }
            break;
        case DISCONNECTED:
#if PRINTS
            spi_print_flash_string(state_names[state]);
#endif
            reset_msg_buffer();
            set_adv_parameters();
            state = ADV_PARAM_SET_W;
            break;
        default:
            break;
    }
}

/**
 * Main function
 *
 * It continously listens for incoming packets. When a packet is received
 * it will trigger the appropriate handler. The handler can trigger an action
 * such as locking or unlocking the door or simply send a command to the 
 * bluetooth module.
 */
int main()
{
    /* Initializations */
    wdt_init(); /* Must be first call or else device will keep resetting */
    ble_hard_reset_init();
    motor_ctrl_init();
    button_init();
    battery_monitor_init();
    ble_wakeup();

    uart_init(BAUD);
    uart_enable();

#if PRINTS
    spi_master_init();
    spi_print_flash_string(str_program_start_i);
#endif
    /* Set BGLib output function pointer to "ble_send_packet" function. */
    bglib_output = ble_send_packet;

    /* Global interrupt enable */
    sei();

    uart_ready_to_receive();

    /* Brings BLE to known state and triggers system boot event. */
    ble_hard_reset();
    state = BLE_BOOT_W;

    while (1) {
        fsm_process();
        if(ble_process_packet() > 0) {
#if PRINTS
            spi_print_flash_string(str_process_packet_e);
#endif
            soft_reset();
        }
        if (request == ROTATE_CW) {
            rotate_motor_cw();
            request = -1;
        } else if (request == ROTATE_CCW) {
            rotate_motor_ccw();
            request = -1;
        }
    }
    return 0;
}

/************************ INTERRUPT SERVICE ROUTINES *************************/

/**
 * Interrupt service routine for wake up.
 *
 * Gets triggered when INT0 is low. Triggers a device wakeup, and disables 
 * the wakeup pin.
 */
ISR(INT0_vect)
{
    /* Disable wake up pin interrupt to prevent cascading. */
    wakeup_pin_disable();
}
