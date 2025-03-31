package tech.robd.emojilogger

/**
 * Factory to configure and provide CallerResolver instances
 */
object CallerResolverFactory {
    private var resolver: ICallerResolver = CallerResolver()

    fun setResolver(newResolver: ICallerResolver) {
        resolver = newResolver
    }

    fun getResolver(): ICallerResolver = resolver
}
