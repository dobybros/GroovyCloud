// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: message.proto

// This CPP symbol can be defined to use imports that match up to the framework
// imports needed when using CocoaPods.
#if !defined(GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS)
 #define GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS 0
#endif

#if GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS
 #import <Protobuf/GPBProtocolBuffers_RuntimeSupport.h>
#else
 #import "GPBProtocolBuffers_RuntimeSupport.h"
#endif

 #import "Message.pbobjc.h"
// @@protoc_insertion_point(imports)

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"

#pragma mark - MessageRoot

@implementation MessageRoot

// No extensions in the file and no imports, so no need to generate
// +extensionRegistry.

@end

#pragma mark - MessageRoot_FileDescriptor

static GPBFileDescriptor *MessageRoot_FileDescriptor(void) {
  // This is called by +initialize so there is no need to worry
  // about thread safety of the singleton.
  static GPBFileDescriptor *descriptor = NULL;
  if (!descriptor) {
    GPB_DEBUG_CHECK_RUNTIME_VERSIONS();
    descriptor = [[GPBFileDescriptor alloc] initWithPackage:@""
                                                     syntax:GPBFileSyntaxProto3];
  }
  return descriptor;
}

#pragma mark - TextMessage

@implementation TextMessage

@dynamic text;

typedef struct TextMessage__storage_ {
  uint32_t _has_storage_[1];
  NSString *text;
} TextMessage__storage_;

// This method is threadsafe because it is initially called
// in +initialize for each subclass.
+ (GPBDescriptor *)descriptor {
  static GPBDescriptor *descriptor = nil;
  if (!descriptor) {
    static GPBMessageFieldDescription fields[] = {
      {
        .name = "text",
        .dataTypeSpecific.className = NULL,
        .number = TextMessage_FieldNumber_Text,
        .hasIndex = 0,
        .offset = (uint32_t)offsetof(TextMessage__storage_, text),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeString,
      },
    };
    GPBDescriptor *localDescriptor =
        [GPBDescriptor allocDescriptorForClass:[TextMessage class]
                                     rootClass:[MessageRoot class]
                                          file:MessageRoot_FileDescriptor()
                                        fields:fields
                                    fieldCount:(uint32_t)(sizeof(fields) / sizeof(GPBMessageFieldDescription))
                                   storageSize:sizeof(TextMessage__storage_)
                                         flags:GPBDescriptorInitializationFlag_None];
    NSAssert(descriptor == nil, @"Startup recursed!");
    descriptor = localDescriptor;
  }
  return descriptor;
}

@end

#pragma mark - ImageMessage

@implementation ImageMessage

@dynamic thumbnailURL;
@dynamic bigURL;
@dynamic originalURL;
@dynamic width;
@dynamic height;
@dynamic size;

typedef struct ImageMessage__storage_ {
  uint32_t _has_storage_[1];
  int32_t width;
  int32_t height;
  int32_t size;
  NSString *thumbnailURL;
  NSString *bigURL;
  NSString *originalURL;
} ImageMessage__storage_;

// This method is threadsafe because it is initially called
// in +initialize for each subclass.
+ (GPBDescriptor *)descriptor {
  static GPBDescriptor *descriptor = nil;
  if (!descriptor) {
    static GPBMessageFieldDescription fields[] = {
      {
        .name = "thumbnailURL",
        .dataTypeSpecific.className = NULL,
        .number = ImageMessage_FieldNumber_ThumbnailURL,
        .hasIndex = 0,
        .offset = (uint32_t)offsetof(ImageMessage__storage_, thumbnailURL),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "bigURL",
        .dataTypeSpecific.className = NULL,
        .number = ImageMessage_FieldNumber_BigURL,
        .hasIndex = 1,
        .offset = (uint32_t)offsetof(ImageMessage__storage_, bigURL),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "originalURL",
        .dataTypeSpecific.className = NULL,
        .number = ImageMessage_FieldNumber_OriginalURL,
        .hasIndex = 2,
        .offset = (uint32_t)offsetof(ImageMessage__storage_, originalURL),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "width",
        .dataTypeSpecific.className = NULL,
        .number = ImageMessage_FieldNumber_Width,
        .hasIndex = 3,
        .offset = (uint32_t)offsetof(ImageMessage__storage_, width),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeInt32,
      },
      {
        .name = "height",
        .dataTypeSpecific.className = NULL,
        .number = ImageMessage_FieldNumber_Height,
        .hasIndex = 4,
        .offset = (uint32_t)offsetof(ImageMessage__storage_, height),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeInt32,
      },
      {
        .name = "size",
        .dataTypeSpecific.className = NULL,
        .number = ImageMessage_FieldNumber_Size,
        .hasIndex = 5,
        .offset = (uint32_t)offsetof(ImageMessage__storage_, size),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeInt32,
      },
    };
    GPBDescriptor *localDescriptor =
        [GPBDescriptor allocDescriptorForClass:[ImageMessage class]
                                     rootClass:[MessageRoot class]
                                          file:MessageRoot_FileDescriptor()
                                        fields:fields
                                    fieldCount:(uint32_t)(sizeof(fields) / sizeof(GPBMessageFieldDescription))
                                   storageSize:sizeof(ImageMessage__storage_)
                                         flags:GPBDescriptorInitializationFlag_None];
#if !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    static const char *extraTextFormatInfo =
        "\003\001\n!!\000\002\004!!\000\003\t!!\000";
    [localDescriptor setupExtraTextInfo:extraTextFormatInfo];
#endif  // !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    NSAssert(descriptor == nil, @"Startup recursed!");
    descriptor = localDescriptor;
  }
  return descriptor;
}

@end

#pragma mark - AudioMessage

@implementation AudioMessage

@dynamic URL;
@dynamic seconds;
@dynamic size;

typedef struct AudioMessage__storage_ {
  uint32_t _has_storage_[1];
  int32_t seconds;
  int32_t size;
  NSString *URL;
} AudioMessage__storage_;

// This method is threadsafe because it is initially called
// in +initialize for each subclass.
+ (GPBDescriptor *)descriptor {
  static GPBDescriptor *descriptor = nil;
  if (!descriptor) {
    static GPBMessageFieldDescription fields[] = {
      {
        .name = "URL",
        .dataTypeSpecific.className = NULL,
        .number = AudioMessage_FieldNumber_URL,
        .hasIndex = 0,
        .offset = (uint32_t)offsetof(AudioMessage__storage_, URL),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "seconds",
        .dataTypeSpecific.className = NULL,
        .number = AudioMessage_FieldNumber_Seconds,
        .hasIndex = 1,
        .offset = (uint32_t)offsetof(AudioMessage__storage_, seconds),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeInt32,
      },
      {
        .name = "size",
        .dataTypeSpecific.className = NULL,
        .number = AudioMessage_FieldNumber_Size,
        .hasIndex = 2,
        .offset = (uint32_t)offsetof(AudioMessage__storage_, size),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeInt32,
      },
    };
    GPBDescriptor *localDescriptor =
        [GPBDescriptor allocDescriptorForClass:[AudioMessage class]
                                     rootClass:[MessageRoot class]
                                          file:MessageRoot_FileDescriptor()
                                        fields:fields
                                    fieldCount:(uint32_t)(sizeof(fields) / sizeof(GPBMessageFieldDescription))
                                   storageSize:sizeof(AudioMessage__storage_)
                                         flags:GPBDescriptorInitializationFlag_None];
#if !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    static const char *extraTextFormatInfo =
        "\001\001!!!\000";
    [localDescriptor setupExtraTextInfo:extraTextFormatInfo];
#endif  // !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    NSAssert(descriptor == nil, @"Startup recursed!");
    descriptor = localDescriptor;
  }
  return descriptor;
}

@end

#pragma mark - ProductMessage

@implementation ProductMessage

@dynamic productId;
@dynamic name;
@dynamic price;
@dynamic imageURL;
@dynamic ownerId;
@dynamic ownerName;
@dynamic activityId;

typedef struct ProductMessage__storage_ {
  uint32_t _has_storage_[1];
  NSString *productId;
  NSString *name;
  NSString *price;
  NSString *imageURL;
  NSString *ownerId;
  NSString *ownerName;
  NSString *activityId;
} ProductMessage__storage_;

// This method is threadsafe because it is initially called
// in +initialize for each subclass.
+ (GPBDescriptor *)descriptor {
  static GPBDescriptor *descriptor = nil;
  if (!descriptor) {
    static GPBMessageFieldDescription fields[] = {
      {
        .name = "productId",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_ProductId,
        .hasIndex = 0,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, productId),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "name",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_Name,
        .hasIndex = 1,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, name),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeString,
      },
      {
        .name = "price",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_Price,
        .hasIndex = 2,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, price),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeString,
      },
      {
        .name = "imageURL",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_ImageURL,
        .hasIndex = 3,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, imageURL),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "ownerId",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_OwnerId,
        .hasIndex = 4,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, ownerId),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "ownerName",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_OwnerName,
        .hasIndex = 5,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, ownerName),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "activityId",
        .dataTypeSpecific.className = NULL,
        .number = ProductMessage_FieldNumber_ActivityId,
        .hasIndex = 6,
        .offset = (uint32_t)offsetof(ProductMessage__storage_, activityId),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
    };
    GPBDescriptor *localDescriptor =
        [GPBDescriptor allocDescriptorForClass:[ProductMessage class]
                                     rootClass:[MessageRoot class]
                                          file:MessageRoot_FileDescriptor()
                                        fields:fields
                                    fieldCount:(uint32_t)(sizeof(fields) / sizeof(GPBMessageFieldDescription))
                                   storageSize:sizeof(ProductMessage__storage_)
                                         flags:GPBDescriptorInitializationFlag_None];
#if !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    static const char *extraTextFormatInfo =
        "\005\001\t\000\004\006!!\000\005\007\000\006\t\000\007\n\000";
    [localDescriptor setupExtraTextInfo:extraTextFormatInfo];
#endif  // !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    NSAssert(descriptor == nil, @"Startup recursed!");
    descriptor = localDescriptor;
  }
  return descriptor;
}

@end

#pragma mark - ContactMessage

@implementation ContactMessage

@dynamic userId;
@dynamic name;
@dynamic thumbnailURL;

typedef struct ContactMessage__storage_ {
  uint32_t _has_storage_[1];
  NSString *userId;
  NSString *name;
  NSString *thumbnailURL;
} ContactMessage__storage_;

// This method is threadsafe because it is initially called
// in +initialize for each subclass.
+ (GPBDescriptor *)descriptor {
  static GPBDescriptor *descriptor = nil;
  if (!descriptor) {
    static GPBMessageFieldDescription fields[] = {
      {
        .name = "userId",
        .dataTypeSpecific.className = NULL,
        .number = ContactMessage_FieldNumber_UserId,
        .hasIndex = 0,
        .offset = (uint32_t)offsetof(ContactMessage__storage_, userId),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "name",
        .dataTypeSpecific.className = NULL,
        .number = ContactMessage_FieldNumber_Name,
        .hasIndex = 1,
        .offset = (uint32_t)offsetof(ContactMessage__storage_, name),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeString,
      },
      {
        .name = "thumbnailURL",
        .dataTypeSpecific.className = NULL,
        .number = ContactMessage_FieldNumber_ThumbnailURL,
        .hasIndex = 2,
        .offset = (uint32_t)offsetof(ContactMessage__storage_, thumbnailURL),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
    };
    GPBDescriptor *localDescriptor =
        [GPBDescriptor allocDescriptorForClass:[ContactMessage class]
                                     rootClass:[MessageRoot class]
                                          file:MessageRoot_FileDescriptor()
                                        fields:fields
                                    fieldCount:(uint32_t)(sizeof(fields) / sizeof(GPBMessageFieldDescription))
                                   storageSize:sizeof(ContactMessage__storage_)
                                         flags:GPBDescriptorInitializationFlag_None];
#if !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    static const char *extraTextFormatInfo =
        "\002\001\006\000\003\n!!\000";
    [localDescriptor setupExtraTextInfo:extraTextFormatInfo];
#endif  // !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    NSAssert(descriptor == nil, @"Startup recursed!");
    descriptor = localDescriptor;
  }
  return descriptor;
}

@end

#pragma mark - CouponMessage

@implementation CouponMessage

@dynamic couponCode;
@dynamic activityName;
@dynamic currency;
@dynamic amount;

typedef struct CouponMessage__storage_ {
  uint32_t _has_storage_[1];
  NSString *couponCode;
  NSString *activityName;
  NSString *currency;
  NSString *amount;
} CouponMessage__storage_;

// This method is threadsafe because it is initially called
// in +initialize for each subclass.
+ (GPBDescriptor *)descriptor {
  static GPBDescriptor *descriptor = nil;
  if (!descriptor) {
    static GPBMessageFieldDescription fields[] = {
      {
        .name = "couponCode",
        .dataTypeSpecific.className = NULL,
        .number = CouponMessage_FieldNumber_CouponCode,
        .hasIndex = 0,
        .offset = (uint32_t)offsetof(CouponMessage__storage_, couponCode),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "activityName",
        .dataTypeSpecific.className = NULL,
        .number = CouponMessage_FieldNumber_ActivityName,
        .hasIndex = 1,
        .offset = (uint32_t)offsetof(CouponMessage__storage_, activityName),
        .flags = (GPBFieldFlags)(GPBFieldOptional | GPBFieldTextFormatNameCustom),
        .dataType = GPBDataTypeString,
      },
      {
        .name = "currency",
        .dataTypeSpecific.className = NULL,
        .number = CouponMessage_FieldNumber_Currency,
        .hasIndex = 2,
        .offset = (uint32_t)offsetof(CouponMessage__storage_, currency),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeString,
      },
      {
        .name = "amount",
        .dataTypeSpecific.className = NULL,
        .number = CouponMessage_FieldNumber_Amount,
        .hasIndex = 3,
        .offset = (uint32_t)offsetof(CouponMessage__storage_, amount),
        .flags = GPBFieldOptional,
        .dataType = GPBDataTypeString,
      },
    };
    GPBDescriptor *localDescriptor =
        [GPBDescriptor allocDescriptorForClass:[CouponMessage class]
                                     rootClass:[MessageRoot class]
                                          file:MessageRoot_FileDescriptor()
                                        fields:fields
                                    fieldCount:(uint32_t)(sizeof(fields) / sizeof(GPBMessageFieldDescription))
                                   storageSize:sizeof(CouponMessage__storage_)
                                         flags:GPBDescriptorInitializationFlag_None];
#if !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    static const char *extraTextFormatInfo =
        "\002\001\n\000\002\014\000";
    [localDescriptor setupExtraTextInfo:extraTextFormatInfo];
#endif  // !GPBOBJC_SKIP_MESSAGE_TEXTFORMAT_EXTRAS
    NSAssert(descriptor == nil, @"Startup recursed!");
    descriptor = localDescriptor;
  }
  return descriptor;
}

@end


#pragma clang diagnostic pop

// @@protoc_insertion_point(global_scope)