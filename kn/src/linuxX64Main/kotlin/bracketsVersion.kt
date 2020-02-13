import cnames.structs.RedisModuleCtx
import cnames.structs.RedisModuleString
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.invoke
import redismodule.REDISMODULE_OK
import redismodule.RedisModule_ReplyWithLongLong

fun bracketsKnVersion(
        ctx: CPointer<RedisModuleCtx>?,
        @Suppress("UNUSED_PARAMETER") argv: CPointer<CPointerVar<RedisModuleString>>?,
        @Suppress("UNUSED_PARAMETER") argc: Int
): Int {
    println("bracketsKnVersion")

    (RedisModule_ReplyWithLongLong!!)(ctx, BRACKETS_KN_VERSION.toLong())

    return REDISMODULE_OK
}
