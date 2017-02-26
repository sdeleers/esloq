# Select the mode: DEBUG or OPTIMIZE
MODE=DEBUG

# MCU settings
MCU=atmega328p
PROGRAMMER=avrisp2
#PROGRAMMER=atmelice_isp
PROGRAMMING_SPEED=1
PROGRAMMING_SPEED_FUSES=200
F_CPU=8000000

# Compilation variables
CC = avr-gcc
#CCFLAGS = -pedantic -Wall -Wextra -Werror -mmcu=$(MCU) -DF_CPU=$(F_CPU)
CCFLAGS = -pedantic -Wall -mmcu=$(MCU) -Werror -DF_CPU=$(F_CPU)
CFLAGS_DEBUG = -g -Os
CFLAGS_OPTIMIZE = -Os -mcall-prologues	
CFLAGS = $(CFLAGS_$(MODE)) $(CCFLAGS)

# Project variables
LIBDIR = lib
SRCDIR = src
OBJDIR = obj
TARGETDIR = bin
INCLUDE = -I $(LIBDIR)/avrnacl-20140813/
LIBS = $(LIBDIR)/avrnacl-20140813/avrnacl_small/obj/libnacl.a
SRCS = $(wildcard $(SRCDIR)/*.c)
OBJS = $(patsubst $(SRCDIR)/%.c, $(OBJDIR)/%.o, $(SRCS))
TARGET = $(TARGETDIR)/main

# The dependency file names.
DEPS = $(OBJS:.o=.d)

# Make options
default: $(TARGET).hex
all: flash

# Dependencies
$(TARGET).hex: $(TARGET).elf 
	avr-objcopy -R .eeprom -O ihex $< $@

$(TARGET).eep: $(TARGET).elf
	avr-objcopy -j .eeprom --change-section-lma .eeprom=0 -O ihex $< $@ 

$(TARGET).elf: $(OBJS)
	@mkdir -p $(TARGETDIR)
	$(CC) $(CFLAGS) -o $@ $^ $(LIBS)

$(OBJDIR)/%.o: $(SRCDIR)/%.c
	@mkdir -p $(OBJDIR)
	$(CC) $(CFLAGS) $(INCLUDE) -MMD -o $@ -c $<

fuses:
	# low power oscillator, no clock division
	sudo avrdude -P usb -c $(PROGRAMMER) -B $(PROGRAMMING_SPEED_FUSES) -p m328p -U lfuse:w:0xEC:m

# Flash the hex file onto the MCU
flash: $(TARGET).hex $(TARGET).eep
	sudo avrdude -P usb -c $(PROGRAMMER) -B $(PROGRAMMING_SPEED) -p m328p -U flash:w:$< -U eeprom:w:$(word 2,$^)

# Cleanup
.PHONY: all, clean, veryclean
clean:
	$(RM) -r $(OBJDIR)

veryclean: clean
	$(RM) -r $(TARGETDIR)

# Let make read the dependency files and handle them.
-include $(DEPS)
