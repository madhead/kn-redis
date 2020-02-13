import cnames.structs.RedisModuleCtx
import cnames.structs.RedisModuleString
import cnames.structs.RedisModuleType
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.cValue
import kotlinx.cinterop.staticCFunction
import redismodule.REDISMODULE_APIVER_1
import redismodule.REDISMODULE_ERR
import redismodule.REDISMODULE_OK
import redismodule.RedisModuleWrapper_CreateCommand
import redismodule.RedisModuleWrapper_CreateDataType
import redismodule.RedisModule_Init

const val BRACKETS_KN_VERSION = 1
lateinit var KNBracketType: CPointer<RedisModuleType>

@ExperimentalUnsignedTypes
@Suppress("unused", "FunctionName")
@CName("RedisModule_OnLoad")
fun RedisModule_OnLoad(
        ctx: CPointer<RedisModuleCtx>?,
        @Suppress("UNUSED_PARAMETER") argv: CPointer<CPointerVar<RedisModuleString>>?,
        @Suppress("UNUSED_PARAMETER") argc: Int
): Int {
    if (!initRedisModule(ctx)) {
        return REDISMODULE_ERR
    }

    if (!registerVersionFunction(ctx)) {
        return REDISMODULE_ERR
    }

    if (!registerBracketsType(ctx)) {
        return REDISMODULE_ERR
    }

    return REDISMODULE_OK
}

private fun initRedisModule(ctx: CPointer<RedisModuleCtx>?) =
        RedisModule_Init(ctx, "brackets.kn", BRACKETS_KN_VERSION, REDISMODULE_APIVER_1) != REDISMODULE_ERR

private fun registerVersionFunction(ctx: CPointer<RedisModuleCtx>?) =
        RedisModuleWrapper_CreateCommand(ctx, "brackets.kn.version", staticCFunction(::bracketsKnVersion), "", 0, 0, 0) != REDISMODULE_ERR

@ExperimentalUnsignedTypes
fun registerBracketsType(ctx: CPointer<RedisModuleCtx>?): Boolean {
    KNBracketType = RedisModuleWrapper_CreateDataType(
            ctx,
            "KNBRACKET",
            BRACKETS_KN_VERSION,
            cValue {
                version = BRACKETS_KN_VERSION.toULong()
                rdb_load = staticCFunction(::bracketsRdbLoad)
                rdb_save = staticCFunction(::bracketsRdbSave)
                aof_rewrite = staticCFunction(::bracketsAofRewrite)
                free = staticCFunction(::bracketsFree)
            }
    ) ?: return false

    if (
            RedisModuleWrapper_CreateCommand(
                    ctx,
                    "brackets.kn.push",
                    staticCFunction(::bracketsKnPush),
                    "write deny-oom",
                    1,
                    1,
                    1
            ) == REDISMODULE_ERR
    ) {
        return false
    }

    if (
            RedisModuleWrapper_CreateCommand(
                    ctx,
                    "brackets.kn.print",
                    staticCFunction(::bracketsKnPrint),
                    "readonly",
                    1,
                    1,
                    1
            ) == REDISMODULE_ERR
    ) {
        return false
    }

    if (
            RedisModuleWrapper_CreateCommand(
                    ctx,
                    "brackets.kn.valid",
                    staticCFunction(::bracketsKnValid),
                    "readonly",
                    1,
                    1,
                    1
            ) == REDISMODULE_ERR
    ) {
        return false
    }

    return true
}
